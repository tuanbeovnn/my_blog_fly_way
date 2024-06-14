package com.myblogbackend.blog.post;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.request.PostRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.myblogbackend.blog.category.CategoryTestApi.makeCategoryForSaving;

public final class PostTestApi {

    public static TagEntity makeTagsForSaving() {
        return TagEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .name(PostTag.AI)
                .build();
    }

    public static PostEntity makePostForSaving() {
        Set<TagEntity> tags = new HashSet<>();
        tags.add(makeTagsForSaving());
        return PostEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .title("Post title A")
                .shortDescription("")
                .thumnails("c4feb3223.png")
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
                .thumnails(List.of("c4feb3223.png"))
                .images(List.of("c4feb3223.png"))
                .tags(Set.of(PostTag.AI, PostTag.INFORMATION))
                .build();
    }


}
