package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.CategoryResponse;

import java.util.UUID;

public interface CategoryService {
    PaginationPage<CategoryResponse> getAllCategories(Integer offset, Integer limited);

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(UUID id, CategoryRequest categoryRequest);

    void updateCategoryStatus(UUID id, Boolean status);
}
