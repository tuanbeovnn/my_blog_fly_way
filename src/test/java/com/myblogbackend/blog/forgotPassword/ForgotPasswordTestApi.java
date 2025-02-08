package com.myblogbackend.blog.forgotPassword;

import com.myblogbackend.blog.enums.OAuth2Provider;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.models.UserVerificationTokenEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public final class ForgotPasswordTestApi {
    public static UserEntity createActiveUser() {
        return UserEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .email("active@example.com")
                .active(true)
                .isPending(false)
                .provider(OAuth2Provider.LOCAL)
                .password("hashed-password")
                .build();
    }
    public static UserEntity createInActiveUser() {
        return UserEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .email("inactive@example.com")
                .active(false)
                .provider(OAuth2Provider.LOCAL)
                .password("hashed-password")
                .build();
    }
    public static UserVerificationTokenEntity createValidToken(UserEntity user) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 30);
        var expiration = calendar.getTime();

        return UserVerificationTokenEntity.builder()
                .verificationToken("d6bbed4f-c00c-4d4b-a7dd-8ce614e40344")
                .user(user)
                .expDate(expiration)
                .build();
    }
}
