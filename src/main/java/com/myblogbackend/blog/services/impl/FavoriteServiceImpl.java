package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.enums.FavoriteObjectType;
import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.models.FavoriteEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.CommentRepository;
import com.myblogbackend.blog.repositories.FavoriteRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.services.FavoriteService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private static final Logger logger = LogManager.getLogger(FavoriteServiceImpl.class);
    private static final String FAVOURITE_COUNT_KEY = "post:favourite:";
    private final FavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CommentRepository commentRepository;

    @Override
    public void persistOrDelete(final UUID postId) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var existingFavorite = favoriteRepository.findByUserIdAndPostId(signedInUser.getId(), postId);
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        existingFavorite.ifPresentOrElse(favorite -> {
            post.setFavourite(Math.max(0, post.getFavourite() - 1));
            favoriteRepository.delete(favorite);
            logger.info("User {} unfavorited post {} successfully", signedInUser.getId(), postId);
        }, () -> {
            post.setFavourite(post.getFavourite() + 1);
            favoriteRepository.save(
                    FavoriteEntity.builder()
                            .user(UserEntity.builder().id(signedInUser.getId()).build())
                            .type(RatingType.LIKE)
                            .post(post)
                            .build()
            );
            logger.info("User {} favorited post {} successfully", signedInUser.getId(), postId);
        });
        postRepository.save(post);
    }

    @Override
    public void persistOrDeleteV2(final UUID postId) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var existingFavorite = favoriteRepository.findByUserIdAndPostId(signedInUser.getId(), postId);

        // Redis key for the post's favorite count
        String redisKey = FAVOURITE_COUNT_KEY + postId;

        existingFavorite.ifPresentOrElse(favorite -> {
            // Unfavorite: decrement the cached count
            redisTemplate.opsForValue().decrement(redisKey);
            favoriteRepository.delete(favorite);
        }, () -> {
            // Favorite: increment the cached count
            redisTemplate.opsForValue().increment(redisKey);
            favoriteRepository.save(
                    FavoriteEntity.builder()
                            .user(UserEntity.builder().id(signedInUser.getId()).build())
                            .type(RatingType.LIKE)
                            .post(PostEntity.builder().id(postId).build())
                            .build()
            );
        });
    }

    @Override
    @Transactional
    public void createFavorite(final UUID targetId, final String objectType, final RatingType type) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var favoriteObject = FavoriteObjectType.valueOf(objectType.toUpperCase());
        switch (favoriteObject) {
            case POST -> handlePostFavorite(signedInUser.getId(), targetId, type);
            case COMMENT -> handleCommentFavorite(signedInUser.getId(), targetId, type);
        }

    }

    private void handlePostFavorite(final UUID userId, final UUID postId, final RatingType type) {
        PostEntity post = postRepository.findById(postId).orElseThrow(() ->
                new RuntimeException("Post not found"));
        var existingFavoritePostByUser = favoriteRepository.findByUserIdAndPostId(userId, postId);

        existingFavoritePostByUser.ifPresentOrElse(favorite -> {
                    if (favoriteRepository.deleteByUserIdAndPostId(userId, postId) > -0) {
                        post.setFavourite(Math.max(0, post.getFavourite() - 1));
                    }
                }
                , () -> {
                    post.setFavourite(post.getFavourite() + 1);
                    var favoriteSaved = FavoriteEntity.builder()
                            .user(UserEntity.builder().id(userId).build())
                            .objectType(FavoriteObjectType.POST)
                            .type(type)
                            .post(post)
                            .build();
                    favoriteRepository.save(favoriteSaved);
                }
        );
    }

    private void handleCommentFavorite(final UUID userId, final UUID commentId, final RatingType type) {
        CommentEntity comment = commentRepository.findById(commentId).orElseThrow(() ->
                new RuntimeException("Comment not found"));
        var existingFavoriteComment = favoriteRepository.findByUserIdAndCommentId(userId, commentId);

        existingFavoriteComment.ifPresentOrElse(favorite -> {
            if (favoriteRepository.deleteByUserIdAndCommentId(userId, commentId) > 0) {
                comment.setLikes(Math.max(0, comment.getLikes() - 1));
            }
        }, () -> {
            comment.setLikes(comment.getLikes() + 1);
            var favoriteSaved = FavoriteEntity.builder()
                    .user(UserEntity.builder().id(userId).build())
                    .objectType(FavoriteObjectType.COMMENT)
                    .type(type)
                    .comment(comment)
                    .build();
            favoriteRepository.save(favoriteSaved);
        });
    }

}
// Flush the cache every 5 minutes
//    @Scheduled(fixedRate = 5 * 60 * 1000)
//    public void flushFavoritesToDatabase() {
//        Optional.ofNullable(redisTemplate.keys(FAVOURITE_COUNT_KEY + "*"))
//                .ifPresent(keys -> keys.forEach(key -> {
//                    UUID postId = UUID.fromString(key.replace(FAVOURITE_COUNT_KEY, ""));
//                    Long cachedCount = (Long) redisTemplate.opsForValue().get(key);
//
//                    if (cachedCount != null) {
//                        postRepository.updateFavouriteCount(postId, cachedCount);
//                        redisTemplate.delete(key);  // Clear the cache entry after flushing
//                    }
//                }));
//
//    }

