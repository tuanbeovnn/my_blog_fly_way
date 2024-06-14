package com.myblogbackend.blog.category;

import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.CategoryResponse;

import java.util.UUID;

public final class CategoryTestApi {

    public static CategoryEntity makeCategoryForSaving(final String name) {
        return CategoryEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .name(name)
                .build();
    }

    public static CategoryRequest prepareCategoryForRequesting() {
        return CategoryRequest.builder()
                .name("Category A")
                .build();
    }

    public static CategoryResponse toCategoryResponse(final CategoryEntity categoryEntity) {
        return CategoryResponse.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .build();
    }

    public static CategoryRequest prepareCategoryForRequestingUpdate(final String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
    }

}
