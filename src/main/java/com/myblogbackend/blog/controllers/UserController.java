package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.config.security.CurrentUser;
import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.UserRoutes;
import com.myblogbackend.blog.request.ChangePasswordRequest;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.request.UserProfileRequest;
import com.myblogbackend.blog.response.ApiResponse;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(UserRoutes.BASE_URL + "/me")
    public ResponseEntity<?> getCurrentUser() {
        UserResponse userProfile = userService.aboutMe();
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userProfile)
                .build();
    }

    @GetMapping(PUBLIC_URL + UserRoutes.BASE_URL + "/byUserName/{userName}")
    public ResponseEntity<?> getUserProfileByUserName(final @PathVariable(value = "userName") String userName) {
        UserResponse userProfile = userService.findUserByUserName(userName);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userProfile)
                .build();
    }

    @PutMapping(UserRoutes.BASE_URL + "/users/changePassWord")
    public ResponseEntity<?> changePassWord(@RequestBody @Valid final ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setMessage("User change password successfully!")
                .build();
    }

    @PutMapping(UserRoutes.BASE_URL + "/user-update")
    public ResponseEntity<?> updateUserProfile(final @RequestBody UserProfileRequest userProfileRequest) {
        UserResponse userProfile = userService.updateProfile(userProfileRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userProfile)
                .build();
    }

    @PutMapping(UserRoutes.BASE_URL + "/logout")
    public ResponseEntity<ApiResponse> logoutUser(final @CurrentUser UserPrincipal currentUser,
                                                  final @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(logOutRequest, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully logged out from the system!"));
    }

}