package com.myblogbackend.blog.specification;

import com.myblogbackend.blog.enums.PostTag;
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
                .and(hasStatusTrue());
    }

    public static Specification<PostEntity> findRelatedArticles(final PostFilterRequest filterRequest) {
        return Specification
                .where(hasTitleContaining(filterRequest.getTitle()))
                .or(hasShortDescContaining(filterRequest.getShortDescription()))
                .or(hasContentContaining(filterRequest.getContent()))
                .and(hasStatusTrue());
    }

    public static Specification<PostEntity> findAllArticles(final PostFilterRequest filterRequest) {
        return Specification
                .where(hasStatusFalse());
    }

    private static Specification<PostEntity> hasStatusTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get(STATUS));
    }

    private static Specification<PostEntity> hasStatusFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get(APPROVE));
    }

    private static Specification<PostEntity> hasCategoryName(final String categoryName) {
        if (categoryName == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, CategoryEntity> categoryJoin = root.join(CATEGORY);
            return criteriaBuilder.equal(categoryJoin.get(NAME), categoryName);
        };
    }

    private static Specification<PostEntity> hasTags(final Set<PostTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, TagEntity> tagJoin = root.join(TAGS);
            return tagJoin.get(NAME).in(tags);
        };
    }

    private static Specification<PostEntity> hasUserId(final UUID userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, UserEntity> userJoin = root.join(USER);
            return criteriaBuilder.equal(userJoin.get(ID), userId);
        };
    }

    private static Specification<PostEntity> hasUserName(final String userName) {
        if (userName == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, UserEntity> userJoin = root.join(USER);
            return criteriaBuilder.equal(userJoin.get("userName"), userName);
        };
    }

    private static Specification<PostEntity> hasTitleContaining(final String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(TITLE)), "%" + title.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasContentContaining(final String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasShortDescContaining(final String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("shortDescription")), "%" + title.toLowerCase() + "%");
    }


}
