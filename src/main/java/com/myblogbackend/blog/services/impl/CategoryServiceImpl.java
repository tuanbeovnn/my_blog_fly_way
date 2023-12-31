package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.CategoryMapper;
import com.myblogbackend.blog.pagination.OffsetPageRequest;
import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.CategoryResponse;
import com.myblogbackend.blog.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);

    @Override
    public PaginationPage<CategoryResponse> getAllCategories(final Integer offset, final Integer limited) {
        try {
            var pageable = new OffsetPageRequest(offset, limited);
            var categoryList = categoryRepository.findAll(pageable);
            var categoryResponse = categoryList.getContent()
                    .stream()
                    .map(categoryMapper::toCategoryResponse)
                    .collect(Collectors.toList());
            logger.info("Get all category with pagination successfully");
            return new PaginationPage<CategoryResponse>()
                    .setRecords(categoryResponse)
                    .setLimit(categoryList.getSize())
                    .setTotalRecords(categoryList.getTotalElements())
                    .setOffset(categoryList.getNumber());
        } catch (Exception e) {
            logger.info("Failed to get all category with pagination", e);
            throw new RuntimeException("Failed to get all category with pagination");
        }
    }

    @Override
    public CategoryResponse getCategoryById(final UUID id) {
        try {
            var category = categoryRepository
                    .findById(id)
                    .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
            logger.info("Get category successfully by id {}", id);
            return categoryMapper.toCategoryResponse(category);
        } catch (Exception e) {
            logger.info("Failed to get category by id", e);
            throw new RuntimeException("Failed to get category by id", e);
        }
    }

    @Override
    public CategoryResponse createCategory(final CategoryRequest categoryRequest) {
        try {
            var category = categoryMapper.toCategoryEntity(categoryRequest);
            var createdCategory = categoryRepository.save(category);
            logger.info("Create category successfully!");
            return categoryMapper.toCategoryResponse(createdCategory);
        } catch (Exception e) {
            logger.info("Failed to create category");
            throw new RuntimeException("Failed to create category", e);
        }
    }

    @Override
    public CategoryResponse updateCategory(final UUID id, final CategoryRequest categoryRequest) {
        try {
            var category = categoryRepository
                    .findById(id)
                    .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
            category.setName(categoryRequest.getName());
            var updatedCategory = categoryRepository.save(category);
            logger.info("Update category successfully");
            return categoryMapper.toCategoryResponse(updatedCategory);
        } catch (Exception e) {
            logger.info("Failed to update category", e);
            throw new RuntimeException("Failed to update category");
        }
    }
}
