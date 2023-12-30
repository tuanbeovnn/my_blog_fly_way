package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.enums.OAuth2Provider;
import com.myblogbackend.blog.exception.TokenRefreshException;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserDeviceEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.UserDeviceRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.DeviceInfoRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.security.JwtProvider;
import com.myblogbackend.blog.services.AuthService;
import com.myblogbackend.blog.strategyPatternV2.MailFactory;
import com.myblogbackend.blog.strategyPatternV2.MailStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    public static final long ONE_HOUR_IN_MILLIS = 3600000;
    private final UsersRepository usersRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final MailStrategy mailStrategy;

    public AuthServiceImpl(UsersRepository usersRepository, AuthenticationManager authenticationManager,
                           JwtProvider jwtProvider, UserDeviceRepository userDeviceRepository,
                           RefreshTokenRepository refreshTokenRepository, PasswordEncoder encoder,
                           UserMapper userMapper, MailFactory mailFactory) {
        this.usersRepository = usersRepository;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userDeviceRepository = userDeviceRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.encoder = encoder;
        this.userMapper = userMapper;
        this.mailStrategy = mailFactory.createStrategy();
    }

    @Override
    public JwtResponse userLogin(final LoginFormRequest loginRequest) {
        var userEntity = usersRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User not found."));

        if (userEntity.getActive()) {
            throw new BlogRuntimeException(ErrorCode.USER_ACCOUNT_IS_NOT_ACTIVE);
        }

        var authentication = authenticateUser(loginRequest);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var jwtToken = jwtProvider.generateJwtToken(authentication);
        var refreshTokenEntity = createRefreshToken(loginRequest, userEntity);
        return new JwtResponse(jwtToken, refreshTokenEntity.getToken(), jwtProvider.getExpiryDuration());
    }

    @Override
    public UserResponse registerUser(final SignUpFormRequest signUpRequest) {
        var userEntityOpt = usersRepository.findByEmail(signUpRequest.getEmail());
        if (userEntityOpt.isPresent()) {
            throw new BlogRuntimeException(ErrorCode.ALREADY_EXIST);
        }
        var newUser = new UserEntity();
        newUser.setEmail(signUpRequest.getEmail());
        newUser.setPassword(encoder.encode(signUpRequest.getPassword()));
        newUser.activate();
        newUser.setName(signUpRequest.getName());
        newUser.setProvider(OAuth2Provider.LOCAL);
        newUser.setIsPending(true);
        var result = usersRepository.save(newUser);
        if (result.getActive()) {
            log.info("Sending activation email to '{}'", result);
            mailStrategy.sendActivationEmail(result);
        }
        return userMapper.toUserDTO(result);
    }

    @Override
    public JwtResponse refreshJwtToken(final TokenRefreshRequest tokenRefreshRequest) {
        var requestRefreshToken = tokenRefreshRequest.getRefreshToken();
        var token = Optional.of(refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshToken -> {
                    verifyExpiration(refreshToken);
                    verifyRefreshAvailability(refreshToken);
                    increaseCount(refreshToken);
                    return refreshToken;
                })
                .map(RefreshTokenEntity::getUserDevice)
                .map(UserDeviceEntity::getUser)
                .map(jwtProvider::generateTokenFromUser)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Missing refresh token in database.Please login again")));
        return new JwtResponse(token.get(), tokenRefreshRequest.getRefreshToken(), jwtProvider.getExpiryDuration());
    }

    @Override
    public ResponseEntity<?> confirmationEmail(String token) throws IOException {
        return null;
    }


//    @Override
//    public ResponseEntity<?> confirmationEmail(String token) throws IOException {
//        UserVerificationTokenEntity verificationToken = tokenRepository.findByVerificationToken(token);
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.setContentType(MediaType.TEXT_HTML);
//        if (verificationToken == null) {
//            InputStream in = getClass().getResourceAsStream("/templates/invalidtoken.html");
//            String result = IOUtils.toString(in, StandardCharsets.UTF_8);
//            return new ResponseEntity<String>(result, responseHeaders, HttpStatus.NOT_FOUND);
//        }
//        UserEntity userEntity = verificationToken.getUser();
//        if (!userEntity.getIsPending()) {
//            InputStream in = getClass().getResourceAsStream("/templates/alreadyconfirmed.html");
//            String result = IOUtils.toString(in, StandardCharsets.UTF_8);
//            return new ResponseEntity<String>(result, responseHeaders, HttpStatus.NOT_FOUND);
//        }
//
//        Calendar cal = Calendar.getInstance();
//        if ((verificationToken.getExpDate().getTime() - cal.getTime().getTime()) <= 0) {
//            InputStream in = getClass().getResourceAsStream("/templates/invalidtoken.html");
//            String result = IOUtils.toString(in, StandardCharsets.UTF_8);
//            return new ResponseEntity<String>(result, responseHeaders, HttpStatus.NOT_FOUND);
//        }
//        userEntity.setIsPending(false);
//        usersRepository.save(userEntity);
//        InputStream in = getClass().getResourceAsStream("/templates/confirmed.html");
//        String result = IOUtils.toString(in, StandardCharsets.UTF_8);
//
//        return new ResponseEntity<String>(result, responseHeaders, HttpStatus.NOT_FOUND);
//    }


//    @Override
//    public ResponseEntity<?> confirmationEmail(String token) throws IOException {
//
//        var verificationToken = tokenRepository.findByVerificationToken(token);
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.setContentType(MediaType.TEXT_HTML);
//        if (verificationToken == null) {
//            InputStream in = getClass().getResourceAsStream("/templates/invalidtoken.html");
//            String result = IOUtils.toString(in, StandardCharsets.UTF_8);
//            return new ResponseEntity<String>(result, responseHeaders, HttpStatus.NOT_FOUND);
//        }
//        return null;
//    }

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
                .expiryDate(Instant.now().plusMillis(ONE_HOUR_IN_MILLIS))
                .token(UUID.randomUUID().toString())
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

    private RefreshTokenEntity createRefreshToken(final LoginFormRequest loginRequest, final UserEntity userEntity) {
        userDeviceRepository.findByUserId(userEntity.getId())
                .map(UserDeviceEntity::getRefreshToken)
                .map(RefreshTokenEntity::getId)
                .ifPresent(refreshTokenRepository::deleteById);

        var userDeviceEntity = createUserDevice(loginRequest.getDeviceInfo());
        var refreshTokenEntity = createRefreshToken();
        userDeviceEntity.setUser(userEntity);
        userDeviceEntity.setRefreshToken(refreshTokenEntity);
        refreshTokenEntity.setUserDevice(userDeviceEntity);
        refreshTokenEntity = refreshTokenRepository.save(refreshTokenEntity);
        return refreshTokenEntity;
    }
}
