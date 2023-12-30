package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.UserRoutes;
import com.myblogbackend.blog.request.LogOutRequest;
import com.myblogbackend.blog.response.ApiResponse;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.security.CurrentUser;
import com.myblogbackend.blog.security.UserPrincipal;
import com.myblogbackend.blog.services.MinioService;
import com.myblogbackend.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + UserRoutes.BASE_URL)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    private final MinioService minioService;

    @PostMapping("/upload")
    public ResponseEntity<?> registerUser(final @ModelAttribute MultipartFile file) {
        var upload = minioService.uploadFile(file);
        return ResponseEntity.ok(upload);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserResponse userProfile = userService.aboutMe();
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/byID/{id}")
    public ResponseEntity<?> getUserProfileById(final @PathVariable(value = "id") UUID id) {
        UserResponse userProfile = userService.findUserById(id);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(final @CurrentUser UserPrincipal currentUser,
                                                  final @Valid @RequestBody LogOutRequest logOutRequest) {
        userService.logoutUser(logOutRequest, currentUser);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully logged out from the system!"));
    }

}