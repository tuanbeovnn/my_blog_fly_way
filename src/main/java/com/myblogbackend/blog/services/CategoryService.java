package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.CategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {
    PageList<CategoryResponse> getAllCategories(Pageable pageable);

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(UUID id, CategoryRequest categoryRequest);

    void updateCategoryStatus(UUID id, Boolean status);

    CategoryResponse getCategoryByName(String categoryName);
}
