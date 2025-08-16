package com.myblogbackend.blog.specification;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.PostType;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.request.PostFilterRequest;

import jakarta.persistence.criteria.Join;

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
                        .or(safe(hasContentContaining(filterRequest.getContent()))));

        return Specification.where(textSearch)
                .and(hasApproved())
                .and(hasStatusTrue());
    }

    public static Specification<PostEntity> findAllArticles(final PostFilterRequest filterRequest) {
        return isPending();
    }

    public static Specification<PostEntity> findRelatedPostsByPostEntity(final PostEntity currentPost) {
        // Build a complex specification to find related posts
        Specification<PostEntity> relatedSpec = Specification.where(null);

        // Exclude the current post itself
        relatedSpec = relatedSpec.and(hasNotPostId(currentPost.getId()));

        // Find posts with similar titles (series detection) - highest priority
        if (currentPost.getTitle() != null) {
            String baseTitle = extractBaseTitle(currentPost.getTitle());
            if (!baseTitle.equals(currentPost.getTitle())) {
                // This looks like it might be part of a series
                relatedSpec = relatedSpec.and(hasSimilarTitle(baseTitle));
            }
        }

        // Find posts by same category (high priority)
        if (currentPost.getCategory() != null) {
            relatedSpec = relatedSpec
                    .or(hasCategoryName(currentPost.getCategory().getName()).and(hasNotPostId(currentPost.getId())));
        }

        // Find posts by same author (medium priority)
        if (currentPost.getUser() != null) {
            relatedSpec = relatedSpec
                    .or(hasUserId(currentPost.getUser().getId()).and(hasNotPostId(currentPost.getId())));
        }

        // Find posts with similar tags (medium priority)
        if (currentPost.getTags() != null && !currentPost.getTags().isEmpty()) {
            var postTags = currentPost.getTags().stream()
                    .map(TagEntity::getName)
                    .collect(java.util.stream.Collectors.toSet());
            relatedSpec = relatedSpec.or(hasTags(postTags).and(hasNotPostId(currentPost.getId())));
        }

        // Only show approved and active posts
        return relatedSpec.and(hasApproved()).and(hasStatusTrue());
    }

    private static Specification<PostEntity> hasNotPostId(final UUID postId) {
        return (root, query, cb) -> cb.notEqual(root.get(ID), postId);
    }

    private static Specification<PostEntity> hasSimilarTitle(final String baseTitle) {
        return isBlank(baseTitle) ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get(TITLE)), "%" + baseTitle.toLowerCase() + "%");
    }

    /**
     * Extracts the base title by removing common series indicators like:
     * "Day X", "Part X", "Chapter X", "Episode X", "Lesson X", etc.
     */
    private static String extractBaseTitle(final String title) {
        if (isBlank(title)) {
            return title;
        }

        // Common patterns for series titles
        String[] patterns = {
                "\\s+(Day|Part|Chapter|Episode|Lesson|Tutorial)\\s+\\d+.*$",
                "\\s+-\\s+(Day|Part|Chapter|Episode|Lesson|Tutorial)\\s+\\d+.*$",
                "\\s+\\(?(Day|Part|Chapter|Episode|Lesson|Tutorial)\\s+\\d+\\)?.*$",
                "\\s+\\[?(Day|Part|Chapter|Episode|Lesson|Tutorial)\\s+\\d+\\]?.*$",
                "\\s*:\\s*(Day|Part|Chapter|Episode|Lesson|Tutorial)\\s+\\d+.*$",
                "\\s+\\d+$", // Just numbers at the end
                "\\s+-\\s+\\d+$", // Dash followed by numbers
                "\\s+\\(\\d+\\)$", // Numbers in parentheses
                "\\s+\\[\\d+\\]$", // Numbers in brackets
        };

        String baseTitle = title;
        for (String pattern : patterns) {
            baseTitle = baseTitle.replaceAll(pattern, "").trim();
        }

        return baseTitle;
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
        return isBlank(title) ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get(TITLE)), "%" + title.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasContentContaining(final String content) {
        return isBlank(content) ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("content")), "%" + content.toLowerCase() + "%");
    }

    private static Specification<PostEntity> hasShortDescContaining(final String shortDesc) {
        return isBlank(shortDesc) ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("shortDescription")),
                        "%" + shortDesc.toLowerCase() + "%");
    }

    // Helper to safely wrap null specs
    private static <T> Specification<T> safe(final Specification<T> spec) {
        return spec == null ? Specification.where(null) : spec;
    }

    private static boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }

}
