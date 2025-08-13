package com.myblogbackend.blog.services;

import com.myblogbackend.blog.enums.RatingType;

import java.util.UUID;

public interface FavoriteService {
    void persistOrDelete(UUID postId);

    void persistOrDeleteV2(UUID postId);
    void createFavorite(UUID targetId, String objectType,
                                  RatingType type);
}
