package com.myblogbackend.blog.controllers;


import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.FavoriteRoutes;

import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + FavoriteRoutes.BASE_URL)
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PutMapping("/{postId}")
    public ResponseEntity<?> favourite(@PathVariable final UUID postId) {
        favoriteService.persistOrDelete(postId);
        return ResponseEntityBuilder
                .getBuilder()
                .build();
    }

    @PostMapping("/{objectType}/{targetId}")
    public ResponseEntity<?> createFavorite(@PathVariable final UUID targetId, @PathVariable final String objectType,
                                            @RequestParam("type") final RatingType type) {
        favoriteService.createFavorite(targetId, objectType, type);
        return ResponseEntityBuilder
                .getBuilder()
                .build();
    }
}