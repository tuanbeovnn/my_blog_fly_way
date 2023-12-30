package com.myblogbackend.blog.services;

import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.security.UserPrincipal;
import com.myblogbackend.blog.strategyPatternV2.NotificationType;

import java.util.UUID;

public interface UserService {
    void logoutUser(LogOutRequest logOutRequest, UserPrincipal userPrincipal);

    UserResponse findUserById(UUID id);

    UserResponse aboutMe();

    void createVerificationToken(UserEntity userEntity, String token, NotificationType notificationType);

}
