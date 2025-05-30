package com.myblogbackend.blog.services;

import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.RoleName;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.request.ChangePasswordRequest;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.UserPostFavoriteResponse;
import com.myblogbackend.blog.response.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void logoutUser(LogOutRequest logOutRequest, UserPrincipal userPrincipal);

    UserResponse findUserByUserName(String userName);

    UserResponse aboutMe();

    UserResponse updateProfile(UserProfileRequest userProfileRequest);

    void changePassword(ChangePasswordRequest changePasswordRequest);

    List<UserPostFavoriteResponse> findUsersWithManyPostsAndHighFavorites(long postThreshold, long favoritesThreshold);

    PageList<UserResponse> getListUsers(Pageable pageable);

    void assignRoleToUser(UUID userId, RoleName roleName);



}
