package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.security.JwtProvider;
import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.FollowType;
import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.exception.UserLogoutException;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.FollowersEntity;
import com.myblogbackend.blog.models.ProfileEntity;
import com.myblogbackend.blog.models.SocialLinks;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.*;
import com.myblogbackend.blog.request.ChangePasswordRequest;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.UserFollowingResponse;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.response.UserResponse.ProfileResponseDTO;
import com.myblogbackend.blog.response.UserResponse.SocialLinksDTO;
import com.myblogbackend.blog.services.UserService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final FollowersRepository followersRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void logoutUser(final LogOutRequest logOutRequest, final UserPrincipal currentUser) {
        var deviceId = logOutRequest.getDeviceInfo().getDeviceId();

        var userDevice = userDeviceRepository.findByUserId(currentUser.getId())
                .filter(device -> device.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(),
                        "Invalid device Id supplied. No matching device found for the given user "));
        // Blacklist the token to invalidate it
        String token = logOutRequest.getToken(); // Ensure the token is passed in the logout request
        long expirationTime = jwtProvider.getTokenExpiryFromJWT(token, TokenType.ACCESS_TOKEN).getTime() - System.currentTimeMillis();

        redisTemplate.opsForValue().set(token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);

        // Remove the device ID from Redis
        redisTemplate.delete(currentUser.getEmail() + ":deviceId"); // Ensure this matches the key structure

        // Delete the refresh token associated with the userDevice
        refreshTokenRepository.deleteById(userDevice.getRefreshToken().getId());

        logger.info("User logout successfully");
    }

    @Transactional
    public UserResponse updateProfile(final UserProfileRequest updateProfileRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var userEntity = getUserById(signedInUser.getId());
        userEntity.setName(updateProfileRequest.getName());

        var profileEntity = userEntity.getProfile();
        if (profileEntity == null) {
            profileEntity = new ProfileEntity();
            profileEntity.setUser(userEntity);
        }

        profileEntity.setBio(updateProfileRequest.getBio());
        profileEntity.setWebsite(updateProfileRequest.getWebsite());
        profileEntity.setLocation(updateProfileRequest.getLocation());
        profileEntity.setAvatarUrl(updateProfileRequest.getAvatarUrl());

        // Update SocialLinks
        var socialLinks = profileEntity.getSocial();
        if (socialLinks == null) {
            socialLinks = new SocialLinks();
        }
        socialLinks.setTwitter(updateProfileRequest.getTwitter());
        socialLinks.setLinkedin(updateProfileRequest.getLinkedin());
        socialLinks.setGithub(updateProfileRequest.getGithub());

        profileEntity.setSocial(socialLinks);

        profileRepository.save(profileEntity);
        usersRepository.save(userEntity);

        // Return the updated user profile information
        var userResponse = userMapper.toUserDTO(userEntity);
        var profileResponse = ProfileResponseDTO.builder()
                .bio(profileEntity.getBio())
                .website(profileEntity.getWebsite())
                .location(profileEntity.getLocation())
                .avatarUrl(profileEntity.getAvatarUrl())
                .social(SocialLinksDTO.builder()
                        .twitter(profileEntity.getSocial().getTwitter())
                        .linkedin(profileEntity.getSocial().getLinkedin())
                        .github(profileEntity.getSocial().getGithub())
                        .build())
                .build();
        userResponse.setProfile(profileResponse);

        return userResponse;
    }

    @Override
    public void changePassword(final ChangePasswordRequest changePasswordRequest) {
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
    }

    @Override
    public UserResponse findUserByUserName(final String userName) {
        var userEntity = usersRepository.findByUserName(userName)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.info("Find user with user name successfully: {}", userName);
        var followers = followersRepository.findByFollowedUserId(userEntity.getId());
        var usersFollowing = getUserFollowingResponses(followers);
        var userResponse = userMapper.toUserDTO(userEntity);
        var userLoggedUUid = getUserId();
        var followType = getUserFollowingType(userLoggedUUid, userEntity.getId());
        userResponse.setFollowType(followType);
        userResponse.setUsersFollowing(usersFollowing);
        logger.info("User response generated successfully for user name: {}", userName);
        return getUserResponse(userEntity, userResponse);
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
        var followers = followersRepository.findByFollowedUserId(signedInUser.getId());
        var usersFollowing = getUserFollowingResponses(followers);
        logger.info("Get sign in user information");
        var userResponse = userMapper.toUserDTO(userEntity);
        userResponse.setUsersFollowing(usersFollowing);
        // Fetch and set profile information
        return getUserResponse(userEntity, userResponse);
    }

    private static UserResponse getUserResponse(final UserEntity userEntity, final UserResponse userResponse) {
        var profileEntity = userEntity.getProfile();
        if (profileEntity != null) {
            var profileResponse = ProfileResponseDTO.builder()
                    .bio(profileEntity.getBio())
                    .website(profileEntity.getWebsite())
                    .location(profileEntity.getLocation())
                    .avatarUrl(profileEntity.getAvatarUrl())
                    .social(SocialLinksDTO.builder()
                            .twitter(profileEntity.getSocial().getTwitter())
                            .linkedin(profileEntity.getSocial().getLinkedin())
                            .github(profileEntity.getSocial().getGithub())
                            .build())
                    .build();
            userResponse.setProfile(profileResponse);
        } else {
            userResponse.setProfile(ProfileResponseDTO.builder()
                    .bio("")
                    .website("")
                    .location("")
                    .avatarUrl("")
                    .social(SocialLinksDTO.builder()
                            .twitter("")
                            .linkedin("")
                            .github("")
                            .build())
                    .build());
        }
        return userResponse;
    }

    private FollowType getUserFollowingType(final UUID followerId, final UUID followedUserId) {
        if (followerId == null || followedUserId == null) {
            return FollowType.UNFOLLOW;
        }
        return followersRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(FollowersEntity::getType)
                .orElse(FollowType.UNFOLLOW);
    }

    private @NotNull List<UserFollowingResponse> getUserFollowingResponses(final List<FollowersEntity> followers) {
        return followers.stream()
                .map(follower -> {
                    var followerUser = usersRepository.findById(follower.getFollower().getId()).orElseThrow();
                    return UserFollowingResponse.builder()
                            .id(followerUser.getId())
                            .name(followerUser.getName())
                            .userName(followerUser.getUserName())
                            .build();
                })
                .toList();
    }

    private UserEntity getUserById(final UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
    }

}
