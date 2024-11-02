package com.myblogbackend.blog.services;

import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.request.ChangePasswordRequest;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.UserPostFavoriteResponse;
import com.myblogbackend.blog.response.UserResponse;

import java.util.List;

public interface UserService {
    void logoutUser(LogOutRequest logOutRequest, UserPrincipal userPrincipal);

    UserResponse findUserByUserName(String userName);

    UserResponse aboutMe();

    UserResponse updateProfile(UserProfileRequest userProfileRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    List<UserPostFavoriteResponse> findUsersWithManyPostsAndHighFavorites(long postThreshold, long favoritesThreshold);



}
