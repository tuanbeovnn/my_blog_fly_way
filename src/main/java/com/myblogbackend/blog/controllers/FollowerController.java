package com.myblogbackend.blog.controllers;


import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.FollowerRoutes;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.FollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + FollowerRoutes.BASE_URL)
@RequiredArgsConstructor
public class FollowerController {
    private final FollowerService followerService;

    @PutMapping("/{userId}")
    public ResponseEntity<?> followingUser(@PathVariable(value = "userId") final UUID userId) {
        followerService.persistOrDelete(userId);
        return ResponseEntityBuilder
                .getBuilder()
                .build();
    }

}