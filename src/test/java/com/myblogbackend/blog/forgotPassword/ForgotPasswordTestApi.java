package com.myblogbackend.blog.forgotPassword;

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
                .password("hashed-password")
                .build();
    }
    public static UserEntity createInActiveUser() {
        return UserEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .email("inactive@example.com")
                .active(false)
                .password("hashed-password")
                .build();
    }
    public static UserVerificationTokenEntity createValidToken(UserEntity user) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 30);
        var expiration = calendar.getTime();

        return UserVerificationTokenEntity.builder()
                .verificationToken("valid-token")
                .user(user)
                .expDate(expiration)
                .build();
    }
}
