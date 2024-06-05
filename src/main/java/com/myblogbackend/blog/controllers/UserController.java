package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.config.security.CurrentUser;
import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.UserRoutes;
import com.myblogbackend.blog.request.LogOutRequest;
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

import java.util.UUID;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + UserRoutes.BASE_URL)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserResponse userProfile = userService.aboutMe();
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userProfile)
                .build();
    }

    @GetMapping("/byID/{id}")
    public ResponseEntity<?> getUserProfileById(final @PathVariable(value = "id") UUID id) {
        UserResponse userProfile = userService.findUserById(id);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userProfile)
                .build();
    }

    @PutMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(final @CurrentUser UserPrincipal currentUser,
                                                  final @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(logOutRequest, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully logged out from the system!"));
    }

}