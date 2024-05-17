package com.myblogbackend.blog.specification;

import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.request.PostFilterRequest;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public class PostSpec {

    public static final String STATUS = "status";
    public static final String STORE = "store";
    public static final String DISTRICT = "district";
    public static final String CITY = "city";
    public static final String TAGS = "tags";
    public static final String ID = "id";
    public static final String NAME = "name";

    private PostSpec() {
    }

    public static Specification<PostEntity> filterBy(final PostFilterRequest postFilterRequest) {
        return Specification
                .where(hasTags(postFilterRequest.getTags()))
                .and(hasStatusTrue());
    }

    private static Specification<PostEntity> hasStatusTrue() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.isTrue(root.get(STATUS));
        };
    }


    private static Specification<PostEntity> hasTags(final Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null; // No filtering by tags
        }
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, TagEntity> tagJoin = root.join(TAGS);
            return tagJoin.get(NAME).in(tags);
        };
    }


}
