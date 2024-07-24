package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.config.security.UserPrincipal;

import java.util.UUID;

public interface UserService {
    void logoutUser(LogOutRequest logOutRequest, UserPrincipal userPrincipal);

    UserResponse findUserById(UUID id);

    UserResponse aboutMe();

    UserResponse updateProfile(UserProfileRequest userProfileRequest);


}
