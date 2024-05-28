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
    public static final String CATEGORY = "category";
    public static final String USER = "user";
    public static final String TAGS = "tags";
    public static final String ID = "id";
    public static final String NAME = "name";

    private PostSpec() {
    }

    public static Specification<PostEntity> filterBy(final PostFilterRequest postFilterRequest) {
        return Specification
                .where(hasTags(postFilterRequest.getTags()))
                .and(hasCategoryId(postFilterRequest.getCategoryId()))
                .and(hasUserId(postFilterRequest.getUserId()))
                .and(hasStatusTrue());
    }

    private static Specification<PostEntity> hasStatusTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get(STATUS));
    }

    private static Specification<PostEntity> hasCategoryId(final UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, CategoryEntity> categoryJoin = root.join(CATEGORY);
            return criteriaBuilder.equal(categoryJoin.get(ID), categoryId);
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


}
