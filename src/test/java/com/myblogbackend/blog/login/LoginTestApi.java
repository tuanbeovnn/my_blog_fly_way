package com.myblogbackend.blog.login;

import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.OAuth2Provider;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserDeviceEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.request.DeviceInfoRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.response.JwtResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

public final class LoginTestApi {
    public static final long ONE_HOUR_IN_MILLIS = 3600000;

    //create device info request for login request mock data
    public static DeviceInfoRequest deviceInfoSaving() {
        return DeviceInfoRequest.builder()
                .deviceId("aaaa-aaaa-aaaa-aaaa")
                .deviceType("BROWER_CHROME")
                .build();
    }

    //create login request
    public static LoginFormRequest loginDataForRequesting() {
        return LoginFormRequest.builder()
                .email("test@gmail.com")
                .password("123123")
                .deviceInfo(deviceInfoSaving())
                .build();
    }

    //create refresh token
    public static RefreshTokenEntity refreshTokenForSaving() {
        return RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .expiryDate(Instant.now().plusMillis(ONE_HOUR_IN_MILLIS))
                .token(UUID.randomUUID().toString())
                .refreshCount(0L)
                .build();
    }

    //create the user entity after find by email successfully
    public static UserEntity userEntityForSaving(final UUID userId, final String password) {
        return UserEntity.builder()
                .id(userId)
                .name("test")
                .email("test@gmail.com")
                .password(password)
                .active(false)
                .provider(OAuth2Provider.LOCAL)
                .build();
    }

    public static UserEntity userEntityBasicInfo() {
        return UserEntity.builder()
                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .name("test")
                .email("test@gmail.com")
                .active(false)
                .provider(OAuth2Provider.LOCAL)
                .build();
    }

    //create the jwt response after login successfully
    public static JwtResponse jwtResponseForSaving(final String jwtToken, final String refreshToken, final long expirationDuration) {
        return JwtResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    //create user device entity
    public static UserDeviceEntity userDeviceForSaving(final DeviceInfoRequest deviceInfoRequest) {
        return UserDeviceEntity.builder()
                .deviceId("aaaa-aaaa-aaaa-aaaa")
                .deviceType("BROWER_CHROME")
                .isRefreshActive(true)
                .build();
    }

    //create refresh token with LoginRequest , User Entity
    public static RefreshTokenEntity createRefreshTokenEntity(final LoginFormRequest loginFormRequest, final UserEntity userEntity) {
        var userDeviceEntity = userDeviceForSaving(deviceInfoSaving());
        var refreshTokenEntity = refreshTokenForSaving();
        userDeviceEntity.setUser(userEntity);
        userDeviceEntity.setRefreshToken(refreshTokenEntity);
        return RefreshTokenEntity.builder()
                .userDevice(userDeviceEntity)
                .build();
    }

    public static Authentication createAuthenticationByLoginRequest(final LoginFormRequest loginFormRequest) {
        return new UsernamePasswordAuthenticationToken(
                loginFormRequest.getEmail(),
                loginFormRequest.getPassword());
    }

    public static String mockJwtToken() {
        return "mockJwtToken";
    }

    public static UserPrincipal userPrincipal() {
        return new UserPrincipal(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "test1@gmail.com", "123", "aaa", null);
    }
}
