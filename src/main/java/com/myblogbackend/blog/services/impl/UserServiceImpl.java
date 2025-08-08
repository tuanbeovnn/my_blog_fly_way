package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.security.JwtProvider;
import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.PostType;
import com.myblogbackend.blog.enums.RoleName;
import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.exception.UserLogoutException;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.ProfileMapper;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.ProfileEntity;
import com.myblogbackend.blog.models.RoleEntity;
import com.myblogbackend.blog.models.SocialLinks;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.ProfileRepository;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.RoleRepository;
import com.myblogbackend.blog.repositories.UserDeviceRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.ChangePasswordRequest;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.UserPostFavoriteResponse;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.services.UserFollowingService;
import com.myblogbackend.blog.services.UserService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private static final String BLACKLISTED_TOKEN = "blacklisted";
    private static final String DEVICE_ID_KEY_SUFFIX = ":deviceId";

    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final UserFollowingService userFollowingService;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final RoleRepository roleRepository;

    @Override
    public void logoutUser(final LogOutRequest logOutRequest, final UserPrincipal currentUser) {
        var deviceId = logOutRequest.getDeviceInfo().getDeviceId();

        var userDevice = userDeviceRepository.findByUserId(currentUser.getId())
                .filter(device -> device.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(),
                        "Invalid device Id supplied. No matching device found for the given user "));
        // Blacklist the token to invalidate it
        String token = logOutRequest.getToken(); // Ensure the token is passed in the logout request
        long expirationTime = jwtProvider.getTokenExpiryFromJWT(token, TokenType.ACCESS_TOKEN).getTime()
                - System.currentTimeMillis();

        redisTemplate.opsForValue().set(token, BLACKLISTED_TOKEN, expirationTime, TimeUnit.MILLISECONDS);

        // Remove the device ID from Redis
        redisTemplate.delete(currentUser.getEmail() + DEVICE_ID_KEY_SUFFIX); // Ensure this matches the key structure

        // Delete the refresh token associated with the userDevice
        refreshTokenRepository.deleteById(userDevice.getRefreshToken().getId());

        logger.info("User logout successfully");
    }

    @Transactional
    public UserResponse updateProfile(final UserProfileRequest updateProfileRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var userEntity = getUserById(signedInUser.getId());

        // Update user name
        userEntity.setName(updateProfileRequest.getName());

        // Get or create profile entity
        var profileEntity = userEntity.getProfile();
        if (profileEntity == null) {
            profileEntity = new ProfileEntity();
            profileEntity.setUser(userEntity);
        }

        // Update profile fields
        updateProfileFields(profileEntity, updateProfileRequest);

        // Update social links safely
        updateSocialLinks(profileEntity, updateProfileRequest);

        // Save entities
        profileRepository.save(profileEntity);
        usersRepository.save(userEntity);

        // Build and return response
        return buildUserResponseWithProfile(userEntity);
    }

    private void updateProfileFields(final ProfileEntity profileEntity, final UserProfileRequest request) {
        profileEntity.setBio(request.getBio());
        profileEntity.setWebsite(request.getWebsite());
        profileEntity.setLocation(request.getLocation());
        profileEntity.setAvatarUrl(request.getAvatarUrl());
    }

    private void updateSocialLinks(final ProfileEntity profileEntity, final UserProfileRequest request) {
        var socialLinks = profileEntity.getSocial();
        if (socialLinks == null) {
            socialLinks = new SocialLinks();
            profileEntity.setSocial(socialLinks);
        }
        socialLinks.setTwitter(request.getTwitter());
        socialLinks.setLinkedin(request.getLinkedin());
        socialLinks.setGithub(request.getGithub());
    }

    private UserResponse buildUserResponseWithProfile(final UserEntity userEntity) {
        var userResponse = userMapper.toUserDTO(userEntity);
        var profileResponse = profileMapper.toProfileResponseDTO(userEntity.getProfile());
        userResponse.setProfile(profileResponse);
        return userResponse;
    }

    @Override
    public void changePassword(final ChangePasswordRequest changePasswordRequest) {
        if (changePasswordRequest == null) {
            throw new BlogRuntimeException(ErrorCode.INVALID_OPERATION);
        }

        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var userEntity = usersRepository.findById(signedInUser.getId())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        if (!encoder.matches(changePasswordRequest.getOldPassword(), userEntity.getPassword())) {
            throw new BlogRuntimeException(ErrorCode.PASSWORD_WRONG);
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new BlogRuntimeException(ErrorCode.PASSWORD_DOES_NOT_MATCH);
        }

        userEntity.setPassword(encoder.encode(changePasswordRequest.getNewPassword()));
        usersRepository.save(userEntity);

        logger.info("Password changed successfully for user: {}", userEntity.getUserName());
    }

    @Override
    public List<UserPostFavoriteResponse> findUsersWithManyPostsAndHighFavorites(final long postThreshold,
                                                                                 final long favoritesThreshold) {
        logger.info("Finding users with more than {} posts and more than {} favorites", postThreshold,
                favoritesThreshold);

        UUID signedInUserId = getUserId();
        var results = postRepository.findUsersWithManyPostsAndHighFavorites(PostType.APPROVED, postThreshold,
                favoritesThreshold);

        // Extract user IDs to batch fetch users
        var userIds = results.stream()
                .map(result -> (UUID) result[0])
                .toList();

        // Batch fetch users to avoid N+1 problem
        var userEntities = usersRepository.findAllById(userIds);
        var userEntityMap = userEntities.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        return results.stream()
                .map(result -> buildUserPostFavoriteResponse(result, userEntityMap, signedInUserId))
                .filter(response -> response != null)
                .toList();
    }

    private UserPostFavoriteResponse buildUserPostFavoriteResponse(
            final Object[] result,
            final Map<UUID, UserEntity> userEntityMap,
            final UUID signedInUserId) {

        var userId = (UUID) result[0];
        var username = (String) result[1];
        var postCount = (Long) result[2];
        var totalFavorites = (Long) result[3];

        var userEntity = userEntityMap.get(userId);
        if (userEntity == null) {
            logger.warn("User not found for ID: {}", userId);
            return null;
        }

        var followers = userFollowingService.getFollowersByUserId(userEntity.getId());
        var usersFollowing = userFollowingService.getUserFollowingResponses(followers);
        var followType = userFollowingService.getUserFollowingType(signedInUserId, userEntity.getId());

        var userResponse = userMapper.toUserDTO(userEntity);
        userResponse.setUsersFollowing(usersFollowing);
        userResponse.setFollowType(followType);

        // Add profile information
        var profileResponse = profileMapper.toProfileResponseDTO(userEntity.getProfile());
        userResponse.setProfile(profileResponse);

        return UserPostFavoriteResponse.builder()
                .userId(userId)
                .username(username)
                .postCount(postCount)
                .totalFavorites(totalFavorites)
                .userDetails(userResponse)
                .build();
    }

    @Override
    public PageList<UserResponse> getListUsers(final Pageable pageable) {
        Page<UserEntity> userEntities = usersRepository.findAll(pageable);
        List<UserResponse> userResponses = userEntities.stream()
                .map(userMapper::toUserDTO).toList();
        return buildPaginatingResponse(userResponses, pageable.getPageSize(), pageable.getPageNumber(),
                userEntities.getTotalElements());

    }

    @Override
    @Transactional
    public void assignRoleToUser(final UUID userId, final RoleName roleName) {
        UserEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.COULD_NOT_FOUND, userId.toString()));

        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.COULD_NOT_FOUND, roleName.toString()));
        user.getRoles().add(role);
        usersRepository.save(user);
    }

    @Override
    public UserResponse findUserByUserName(final String userName) {
        var userEntity = usersRepository.findByUserName(userName)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        logger.info("Find user with user name successfully: {}", userName);

        UUID userLoggedUUid = getUserId();
        var userResponse = buildCompleteUserResponse(userEntity, userLoggedUUid);

        logger.info("User response generated successfully for user name: {}", userName);
        return userResponse;
    }

    private UUID getUserId() {
        try {
            var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
            return signedInUser.getId();
        } catch (Exception e) {
            logger.info("No user logged in, proceeding without user context");
            return null;
        }
    }

    @Override
    public UserResponse aboutMe() {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var userEntity = getUserById(signedInUser.getId());

        logger.info("Get sign in user information");

        return buildCompleteUserResponse(userEntity, signedInUser.getId());
    }

    private UserResponse buildCompleteUserResponse(final UserEntity userEntity, final UUID currentUserId) {
        var followers = userFollowingService.getFollowersByUserId(userEntity.getId());
        var usersFollowing = userFollowingService.getUserFollowingResponses(followers);
        var followType = userFollowingService.getUserFollowingType(currentUserId, userEntity.getId());

        var userResponse = userMapper.toUserDTO(userEntity);
        userResponse.setUsersFollowing(usersFollowing);
        userResponse.setFollowType(followType);

        // Add profile information
        var profileResponse = profileMapper.toProfileResponseDTO(userEntity.getProfile());
        userResponse.setProfile(profileResponse);

        return userResponse;
    }

    private UserEntity getUserById(final UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
    }

    private PageList<UserResponse> buildPaginatingResponse(final List<UserResponse> responses, final int pageSize,
                                                           final int currentPage, final long total) {
        return PageList.<UserResponse>builder()
                .records(responses)
                .limit(pageSize)
                .offset(currentPage)
                .totalRecords(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
    }

}
