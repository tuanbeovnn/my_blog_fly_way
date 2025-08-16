package com.myblogbackend.blog.post;

import static com.myblogbackend.blog.category.CategoryTestApi.makeCategoryForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.PostType;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.request.PostRequest;

public final class PostTestApi {

    public static TagEntity makeTagsForSaving() {
        return TagEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .name(PostTag.AI)
                .build();
    }

    public static PostEntity makePostForSaving(final String title, final String content) {
        Set<TagEntity> tags = new HashSet<>();
        tags.add(makeTagsForSaving());
        return PostEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .title(title)
                .content(content)
                .shortDescription("")
                .thumbnails("c4feb3223.png")
                .images("c4feb3223.png")
                .category(makeCategoryForSaving("Category A"))
                .tags(tags)
                .build();
    }

    public static PostRequest preparePostForRequest() {
        return PostRequest.builder()
                .title("Post title A")
                .categoryId(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .shortDescription("Post title A")
                .thumbnails(List.of("c4feb3223.png"))
                .tags(Set.of(PostTag.AI, PostTag.APACHE))
                .build();
    }

    public static List<PostEntity> preparePostsEntitySaving() {
        return List.of(
                makePostForSaving("Title B", "Description B"),
                makePostForSaving("Title A", "Description A"));
    }

    // Test data for Related Posts API
    public static PostEntity makeSeriesPost(String title, String content, String categoryName) {
        Set<TagEntity> tags = new HashSet<>();
        tags.add(makeTagsForSaving());
        return PostEntity.builder()
                .id(UUID.randomUUID())
                .title(title)
                .content(content)
                .shortDescription("Short description for " + title)
                .thumbnails("thumbnail.png")
                .category(makeCategoryForSaving(categoryName))
                .tags(tags)
                .status(true)
                .postType(PostType.APPROVED)
                .user(userEntityBasicInfo())
                .build();
    }

    public static List<PostEntity> prepareSeriesPostsForTesting() {
        return List.of(
                makeSeriesPost("Introduction to Java Programming Day 1", "Day 1 content", "Programming"),
                makeSeriesPost("Introduction to Java Programming Day 2", "Day 2 content", "Programming"),
                makeSeriesPost("Introduction to Java Programming Day 3", "Day 3 content", "Programming"),
                makeSeriesPost("React Tutorial Part 1", "React Part 1 content", "Frontend"),
                makeSeriesPost("React Tutorial Part 2", "React Part 2 content", "Frontend"),
                makeSeriesPost("Python Basics Chapter 1", "Python Chapter 1 content", "Programming"),
                makeSeriesPost("Spring Boot Guide Episode 1", "Spring Boot content", "Backend"),
                makeSeriesPost("Different Topic Post", "Unrelated content", "General"));
    }

    public static PostEntity makeCurrentPost() {
        return makeSeriesPost("Introduction to Java Programming Day 1", "Current post content", "Programming");
    }

    public static List<PostEntity> prepareRelatedPostsByCategory() {
        return List.of(
                makeSeriesPost("Advanced Java Concepts", "Advanced Java content", "Programming"),
                makeSeriesPost("Java Best Practices", "Best practices content", "Programming"),
                makeSeriesPost("Object-Oriented Programming", "OOP content", "Programming"));
    }

    public static List<PostEntity> prepareRelatedPostsByAuthor() {
        var author = userEntityBasicInfo();
        return List.of(
                PostEntity.builder()
                        .id(UUID.randomUUID())
                        .title("Author's Other Post 1")
                        .content("Content 1")
                        .shortDescription("Short desc 1")
                        .thumbnails("thumbnail1.png")
                        .images("image1.png")
                        .category(makeCategoryForSaving("Different Category"))
                        .tags(Set.of(makeTagsForSaving()))
                        .user(author)
                        .status(true)
                        .postType(PostType.APPROVED)
                        .build(),
                PostEntity.builder()
                        .id(UUID.randomUUID())
                        .title("Author's Other Post 2")
                        .content("Content 2")
                        .shortDescription("Short desc 2")
                        .thumbnails("thumbnail2.png")
                        .images("image2.png")
                        .category(makeCategoryForSaving("Another Category"))
                        .tags(Set.of(makeTagsForSaving()))
                        .user(author)
                        .status(true)
                        .postType(PostType.APPROVED)
                        .build());
    }

}
