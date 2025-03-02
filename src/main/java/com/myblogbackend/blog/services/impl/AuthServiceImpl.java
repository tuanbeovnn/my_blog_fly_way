package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.kafka.KafkaTopicManager;
import com.myblogbackend.blog.config.mail.EmailProperties;
import com.myblogbackend.blog.config.security.JwtProvider;
import com.myblogbackend.blog.enums.OAuth2Provider;
import com.myblogbackend.blog.enums.RoleName;
import com.myblogbackend.blog.exception.TokenRefreshException;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.feign.OutboundIdentityClient;
import com.myblogbackend.blog.feign.OutboundUserClient;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.RoleEntity;
import com.myblogbackend.blog.models.UserDeviceEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.models.UserVerificationTokenEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.RoleRepository;
import com.myblogbackend.blog.repositories.UserDeviceRepository;
import com.myblogbackend.blog.repositories.UserTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.DeviceInfoRequest;
import com.myblogbackend.blog.request.ExchangeTokenRequest;
import com.myblogbackend.blog.request.ForgotPasswordRequest;
import com.myblogbackend.blog.request.LoginFormOutboundRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.MailRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.services.AuthService;
import com.myblogbackend.blog.utils.GsonUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.myblogbackend.blog.utils.SlugUtil.splitFromEmail;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);
    public static final long ONE_DAY_IN_MILLIS = 86400000;
    public static final String JSON = "json";
    public static final String TEMPLATES_INVALIDTOKEN_HTML = "/templates/invalidtoken.html";
    public static final String TEMPLATES_ALREADYCONFIRMED_HTML = "/templates/alreadyconfirmed.html";
    public static final String TEMPLATES_EMAIL_ACTIVATED_HTML = "/templates/emailActivated.html";
    private static final int EXPIRATION_TIME_MINUTES = 15;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    private String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    private String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    private String REDIRECT_URI;
    private static final String GRANT_TYPE = "authorization_code";
    private final UsersRepository usersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final UserTokenRepository userTokenRepository;
    private final RoleRepository roleRepository;
    private final OutboundIdentityClient outboundIdentityClient;
    private final EmailProperties emailProperties;
    private final OutboundUserClient outboundUserClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicManager kafkaTopicManager;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long EXPIRATION_TIME = 24;
    private static final String EMAIL_PREFIX = "email_verification:";

    @Override
    public JwtResponse userLogin(final LoginFormRequest loginRequest) {
        var userEntity = usersRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.USER_COULD_NOT_FOUND));

        if (!userEntity.getActive()) {
            throw new BlogRuntimeException(ErrorCode.USER_ACCOUNT_IS_NOT_ACTIVE);
        }
        var authentication = authenticateUser(loginRequest);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        var jwtToken = jwtProvider.generateJwtToken(userEntity, loginRequest.getDeviceInfo().getDeviceId());
        var refreshTokenEntity = createRefreshToken(loginRequest.getDeviceInfo(), userEntity);
        return new JwtResponse(jwtToken, refreshTokenEntity.getToken());
    }

    @Override
    public void registerUserV2(final SignUpFormRequest signUpRequest) {
        var email = signUpRequest.getEmail();

        if (usersRepository.existsByEmail(email)) {
            throw new BlogRuntimeException(ErrorCode.ALREADY_EXIST);
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey(EMAIL_PREFIX + email))) {
            throw new BlogRuntimeException(ErrorCode.ALREADY_EXIST);
        }

        usersRepository.findByEmailAndIsPendingTrue(email).ifPresent(usersRepository::delete);

        var token = UUID.randomUUID().toString();

        saveVerificationToken(token, signUpRequest);

        var confirmationLink = String.format(emailProperties.getRegistrationConfirmation().getBaseUrl(), token);
        var mailRequest = createMailRequest(email, confirmationLink);

        kafkaTemplate.send(kafkaTopicManager.getNotificationRegisterTopic(), mailRequest);

        logger.info("User registration request stored in Redis for email '{}'", email);
    }

    public void saveVerificationToken(final String token, final SignUpFormRequest user) {
        var emailKey = EMAIL_PREFIX + user.getEmail();
        var userJson = GsonUtils.objectToString(user);
        redisTemplate.opsForValue().set(token, userJson, EXPIRATION_TIME, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(emailKey, token, EXPIRATION_TIME, TimeUnit.HOURS);
    }

    public SignUpFormRequest getUserByToken(final String token) {
        var userJson = redisTemplate.opsForValue().get(token);
        return GsonUtils.stringToObject(userJson, SignUpFormRequest.class);
    }

    public void deleteToken(final String token, final String email) {
        redisTemplate.delete(token);
        redisTemplate.delete(EMAIL_PREFIX + email);
    }

    @Override
    public ResponseEntity<?> confirmationEmail(final String token) throws IOException {
        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);

        var signUpRequest = getUserByToken(token);

        if (signUpRequest == null) {
            return loadHtmlTemplate(TEMPLATES_INVALIDTOKEN_HTML, responseHeaders);
        }

        var existingUser = usersRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getIsPending()) {
            return loadHtmlTemplate(TEMPLATES_ALREADYCONFIRMED_HTML, responseHeaders);
        }

        usersRepository.findByEmailAndIsPendingTrue(signUpRequest.getEmail()).ifPresent(usersRepository::delete);

        var newUser = UserEntity.builder()
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .active(true)
                .isPending(false)
                .followers(0L)
                .name(signUpRequest.getName())
                .userName(splitFromEmail(signUpRequest.getEmail()))
                .provider(OAuth2Provider.LOCAL)
                .roles(Set.of(getUserRole()))
                .build();

        usersRepository.save(newUser);
        logger.info("User activated successfully: {}", newUser);

        deleteToken(token, signUpRequest.getEmail());

        return loadHtmlTemplate(TEMPLATES_EMAIL_ACTIVATED_HTML, responseHeaders);

    }

    public void sendEmailForgotPassword(final String email) {
        var userEntity = usersRepository.findByEmail(email).orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        var token = createVerificationToken(userEntity);
        // here, later on, using front-end url link to load a creating new password form (react router)
        var confirmationLink = String.format(emailProperties.getForgotPasswordConfirmation().getBaseUrl(), token);
        var mailRequest = createMailRequest(userEntity.getEmail(), confirmationLink);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);

        try {
            kafkaTemplate.send(kafkaTopicManager.getNotificationForgotPasswordTopic(), mailRequest);
        } catch (Exception e) {
            throw new BlogRuntimeException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void handleForgotPassword(final ForgotPasswordRequest forgotPasswordRequest,
                                     final String token) throws IOException {
        UserEntity userEntity = checkValidUserLogin(forgotPasswordRequest.getEmail());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        if (!userEntity.getActive()) {
            throw new BlogRuntimeException(ErrorCode.USER_ACCOUNT_IS_NOT_ACTIVE);
        }
        logger.info("Sending forgot password email to '{}'", userEntity.getEmail());
        var userVerificationTokenFound = userTokenRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.COULD_NOT_FOUND));

        if (userVerificationTokenFound == null || isTokenExpired(userVerificationTokenFound)) {
            throw new BlogRuntimeException(ErrorCode.INVALID_TOKEN);
        }
        UserEntity user = userVerificationTokenFound.getUser();
        user.setPassword(encoder.encode(forgotPasswordRequest.getNewPassword()));
        usersRepository.save(user);
    }


    private RoleEntity getUserRole() {
        return roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
    }

    private MailRequest createMailRequest(final String email, final String confirmationLink) {
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("confirmationLink", confirmationLink);
        MailRequest mailRequest = new MailRequest();
        mailRequest.setTo(email);
        mailRequest.setTemplateVariables(templateVariables);
        return mailRequest;
    }


    @Override
    public JwtResponse outboundAuthentication(final LoginFormOutboundRequest loginFormOutboundRequest) {
        try {
            logger.info("Starting OAuth authentication for user. Code: {}, Client ID: {}, Redirect URI: {}",
                    loginFormOutboundRequest.getCode(), CLIENT_ID, REDIRECT_URI);

            var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                    .code(loginFormOutboundRequest.getCode())
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUri(REDIRECT_URI)
                    .grantType(GRANT_TYPE)
                    .build());

            if (response == null || response.getAccessToken() == null) {
                logger.error("Failed to retrieve access token from Google.");
                throw new BlogRuntimeException(ErrorCode.GOOGLE_AUTH_FAILED);
            }

            logger.info("Access token successfully received from Google: {}", response.getAccessToken());

            var userInfo = outboundUserClient.getUserInfo(JSON, response.getAccessToken());
            logger.info("User info retrieved from Google: Email: {}, Name: {}", userInfo.getEmail(), userInfo.getName());

            var user = usersRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> createNewUser(userInfo.getEmail(), userInfo.getName()));

            if (user.getProvider() == OAuth2Provider.LOCAL) {
                logger.error("User account is registered locally, cannot authenticate with Google.");
                throw new BlogRuntimeException(ErrorCode.ACCOUNT_CREATED_LOCAL);
            }

            var jwtToken = jwtProvider.generateJwtToken(user, loginFormOutboundRequest.getDeviceInfo().getDeviceId());

            var refreshTokenEntity = createRefreshToken(loginFormOutboundRequest.getDeviceInfo(), user);

            return JwtResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshTokenEntity.getToken())
                    .build();
        } catch (FeignException.BadRequest e) {
            logger.error("Invalid grant error during token exchange: {}", e.responseBody());
            throw new BlogRuntimeException(ErrorCode.INVALID_AUTHORIZATION_CODE, e);
        } catch (Exception e) {
            logger.error("Error occurred during authentication process: {}", e.getMessage(), e);
            throw new BlogRuntimeException(ErrorCode.GENERAL_AUTH_ERROR, e);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(final ForgotPasswordRequest forgotPasswordDto) {
        UserEntity userEntity = checkValidUserLogin(forgotPasswordDto.getEmail());
        if (!userEntity.getActive()) {
            logger.info("Sending activation email to '{}'", userEntity);
            var newPassword = UUID.randomUUID().toString().substring(0, 8);
            userEntity.setPassword(encoder.encode(newPassword));
            UserEntity result = usersRepository.save(userEntity);
//            createVerificationToken(result, newPassword, EMAIL_FORGOT_PASSWORD);
            try {
//                mailStrategy.sendForgotPasswordEmail(result, newPassword);
            } catch (Exception e) {
                // Roll back the transaction if email sending fails
                logger.error("Failed to send activation email", e);
                throw new BlogRuntimeException(ErrorCode.EMAIL_SEND_FAILED);
            }
        }
    }


    private UserEntity checkValidUserLogin(final String email) {
        UserEntity userEntity = usersRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User not found."));
        if (!userEntity.getProvider().equals(OAuth2Provider.LOCAL)) {
            throw new RuntimeException("Email already existed with sign in by oauth2 method!");
        }
        return userEntity;
    }

    private UserEntity createNewUser(final String email, final String name) {
        var roles = new HashSet<RoleEntity>();
        var userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.COULD_NOT_FOUND));
        roles.add(userRole);
        var user = UserEntity.builder()
                .provider(OAuth2Provider.GOOGLE)
                .email(email)
                .followers(0L)
                .name(name)
                .userName(splitFromEmail(email))
                .active(false)
                .isPending(false)
                .roles(roles)
                .build();
        usersRepository.save(user);
        return user;
    }

    @Override
    public JwtResponse refreshJwtToken(final TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        // Retrieve the refresh token from the database
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Missing refresh token in database. Please login again"));

        // Verify expiration and availability of the refresh token
        verifyExpiration(refreshToken);
        verifyRefreshAvailability(refreshToken);
        increaseCount(refreshToken); // Assuming this increments usage count or something similar

        // Generate new access token using user details from the refresh token
        UserEntity userEntity = refreshToken.getUserDevice().getUser();
        String newAccessToken = jwtProvider.generateJwtToken(userEntity, refreshToken.getUserDevice().getDeviceId());

        return new JwtResponse(newAccessToken, requestRefreshToken);
    }


    private void verifyExpiration(final RefreshTokenEntity token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new TokenRefreshException(token.getToken(), "Expired token. Please issue a new request");
        }
    }

    private void verifyRefreshAvailability(final RefreshTokenEntity refreshTokenEntity) {
        var userDeviceEntity = userDeviceRepository.findByRefreshToken(refreshTokenEntity)
                .orElseThrow(() -> new TokenRefreshException(refreshTokenEntity.getToken(), "No device found for the matching token. Please login again"));
        if (!userDeviceEntity.getIsRefreshActive()) {
            throw new TokenRefreshException(refreshTokenEntity.getToken(), "Refresh blocked for the device. Please login through a different device");
        }
    }

    private void increaseCount(final RefreshTokenEntity refreshTokenEntity) {
        refreshTokenEntity.incrementRefreshCount();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private UserDeviceEntity createUserDevice(final DeviceInfoRequest deviceInfoRequest) {
        return UserDeviceEntity.builder()
                .deviceId(deviceInfoRequest.getDeviceId())
                .deviceType(deviceInfoRequest.getDeviceType())
                .isRefreshActive(true)
                .build();
    }

    private RefreshTokenEntity createRefreshToken() {
        return RefreshTokenEntity.builder()
                .expiryDate(Instant.now().plusMillis(ONE_DAY_IN_MILLIS))
                .token(jwtProvider.generateRefreshTokenToken(Instant.now().plusMillis(ONE_DAY_IN_MILLIS)))
                .refreshCount(0L)
                .build();
    }

    private Authentication authenticateUser(final LoginFormRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BlogRuntimeException(ErrorCode.UNAUTHORIZED);
        }
    }

    private RefreshTokenEntity createRefreshToken(final DeviceInfoRequest deviceInfoRequest, final UserEntity userEntity) {
        userDeviceRepository.findByUserId(userEntity.getId())
                .map(UserDeviceEntity::getRefreshToken)
                .map(RefreshTokenEntity::getId)
                .ifPresent(refreshTokenRepository::deleteById);

        var userDeviceEntity = createUserDevice(deviceInfoRequest);
        var refreshTokenEntity = createRefreshToken();
        userDeviceEntity.setUser(userEntity);
        userDeviceEntity.setRefreshToken(refreshTokenEntity);
        refreshTokenEntity.setUserDevice(userDeviceEntity);
        refreshTokenEntity = refreshTokenRepository.save(refreshTokenEntity);
        return refreshTokenEntity;
    }

    private boolean isTokenExpired(final UserVerificationTokenEntity verificationToken) {
        long timeDifference = verificationToken.getExpDate().getTime() - Calendar.getInstance().getTime().getTime();
        return timeDifference <= 0;
    }

    private ResponseEntity<String> loadHtmlTemplate(final String templatePath,
                                                    final HttpHeaders headers) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(templatePath)) {
            if (in != null) {
                String result = IOUtils.toString(in, StandardCharsets.UTF_8);
                return new ResponseEntity<>(result, headers, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public String createVerificationToken(final UserEntity user) {
        // Generate a new token
        var token = UUID.randomUUID().toString();
        var expirationDate = calculateExpirationDate();
        var verificationToken = UserVerificationTokenEntity.builder()
                .verificationToken(token)
                .expDate(expirationDate)
                .user(user)
                .build();
        userTokenRepository.save(verificationToken);

        return token;
    }

    private Date calculateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, AuthServiceImpl.EXPIRATION_TIME_MINUTES);
        return calendar.getTime();
    }

    private void saveDeviceToRedis(final LoginFormRequest loginRequest, final UserEntity userEntity) {
        redisTemplate.opsForValue().set(userEntity.getEmail() + ":deviceId", loginRequest.getDeviceInfo().getDeviceId(),
                Duration.ofMinutes(30));
    }

    private void checkLoginAnotherDevices(final LoginFormRequest loginRequest, final UserEntity userEntity) {
        // Check if the user is already logged in from another device
        String existingDeviceId = redisTemplate.opsForValue().get(userEntity.getEmail() + ":deviceId");
        if (existingDeviceId != null && !existingDeviceId.equals(loginRequest.getDeviceInfo().getDeviceId())) {
            throw new BlogRuntimeException(ErrorCode.USER_ALREADY_LOGGED_IN); // Define appropriate error code
        }
    }
}
