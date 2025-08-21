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

    @PutMapping()
    public ResponseEntity<?> createFavorite(@RequestParam("objectType") final String objectType,
                                            @RequestParam("type") final RatingType type,
                                            @RequestParam("targetId") final UUID targetId,
                                            @RequestParam("postId") final UUID postId) {
        favoriteService.createFavorite(targetId, objectType, type, postId);
        return ResponseEntityBuilder
                .getBuilder()
                .build();
    }
}