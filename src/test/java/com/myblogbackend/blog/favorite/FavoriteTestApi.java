package com.myblogbackend.blog.favorite;

import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.models.FavoriteEntity;

import java.util.UUID;

import static com.myblogbackend.blog.comment.CommentTestApi.makeCommentForSaving;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;

public class FavoriteTestApi {
    public static FavoriteEntity makeFavoritePostForSaving() {
        return FavoriteEntity.builder()
                .id(UUID.fromString("7b58db7c-2c45-4850-b85d-cd4bdc261fb4"))
                .post(makePostForSaving("Title B", "Description B"))
                .type(RatingType.LIKE)
                .build();
    }
    public static FavoriteEntity makeFavoriteCommentForSaving() {
        return FavoriteEntity.builder()
                .id(UUID.fromString("7b58db7c-2c45-4850-b85d-cd4bdc261fb4"))
                .comment(makeCommentForSaving("Insight article"))
                .type(RatingType.LIKE)
                .build();
    }
}
