package com.myblogbackend.blog.specification;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.PostType;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.request.PostFilterRequest;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;


public class PostSpec {

    public static final String STATUS = "status";

    public static final String APPROVE = "approved";
    public static final String CATEGORY = "category";
    public static final String USER = "user";
    public static final String TAGS = "tags";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TITLE = "title";

    private PostSpec() {
    }

    public static Specification<PostEntity> filterBy(final PostFilterRequest postFilterRequest) {
        return Specification
                .where(hasTags(postFilterRequest.getTags()))
                .and(hasCategoryName(postFilterRequest.getCategoryName()))
                .and(hasUserId(postFilterRequest.getUserId()))
                .and(hasUserName(postFilterRequest.getUserName()))
                .and(hasApproved())
                .and(hasStatusTrue());
    }

    public static Specification<PostEntity> findRelatedArticles(final PostFilterRequest filterRequest) {
        Specification<PostEntity> textSearch = Specification.where(
                safe(hasTitleContaining(filterRequest.getTitle()))
                        .or(safe(hasShortDescContaining(filterRequest.getShortDescription())))
                        .or(safe(hasContentContaining(filterRequest.getContent())))
        );

        return Specification.where(textSearch)
                .and(hasApproved())
                .and(hasStatusTrue());
    }

    public static Specification<PostEntity> findAllArticles(final PostFilterRequest filterRequest) {
        return isPending();
    }

    private static Specification<PostEntity> hasStatusTrue() {
        return (root, query, cb) -> cb.isTrue(root.get(STATUS));
    }

    private static Specification<PostEntity> isPending() {
        return (root, query, cb) -> cb.equal(root.get("postType"), PostType.PENDING);
    }

    private static Specification<PostEntity> hasApproved() {
        return (root, query, cb) -> cb.equal(root.get("postType"), PostType.APPROVED);
    }

    private static Specification<PostEntity> hasCategoryName(final String categoryName) {
        return isBlank(categoryName) ? null : (root, query, cb) -> {
            Join<PostEntity, CategoryEntity> categoryJoin = root.join(CATEGORY);
            return cb.equal(categoryJoin.get(NAME), categoryName);
        };
    }

    private static Specification<PostEntity> hasTags(final Set<PostTag> tags) {
        return (tags == null || tags.isEmpty()) ? null : (root, query, cb) -> {
            Join<PostEntity, TagEntity> tagJoin = root.join(TAGS);
            return tagJoin.get(NAME).in(tags);
        };
    }

    private static Specification<PostEntity> hasUserId(final UUID userId) {
        return userId == null ? null : (root, query, cb) -> {
            Join<PostEntity, UserEntity> userJoin = root.join(USER);
            return cb.equal(userJoin.get(ID), userId);
        };
    }

    private static Specification<PostEntity> hasUserName(final String userName) {
        return isBlank(userName) ? null : (root, query, cb) -> {
            Join<PostEntity, UserEntity> userJoin = root.join(USER);
            return cb.equal(userJoin.get("userName"), userName);
        };
    }

    private static Specification<PostEntity> hasTitleContaining(final String title) {
        return isBlank(title) ? null : (root, query, cb) ->
                cb.like(cb.lower(root.get(TITLE)), "%" + title.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasContentContaining(final String content) {
        return isBlank(content) ? null : (root, query, cb) ->
                cb.like(cb.lower(root.get("content")), "%" + content.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasShortDescContaining(final String shortDesc) {
        return isBlank(shortDesc) ? null : (root, query, cb) ->
                cb.like(cb.lower(root.get("shortDescription")), "%" + shortDesc.toLowerCase() + "%");
    }

    // Helper to safely wrap null specs
    private static <T> Specification<T> safe(final Specification<T> spec) {
        return spec == null ? Specification.where(null) : spec;
    }

    private static boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }


}
