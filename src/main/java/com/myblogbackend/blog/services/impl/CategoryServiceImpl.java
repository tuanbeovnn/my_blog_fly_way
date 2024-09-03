package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.CategoryMapper;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.CategoryResponse;
import com.myblogbackend.blog.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.myblogbackend.blog.utils.SlugUtil.makeSlug;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LogManager.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final PostRepository postRepository;

    @Override
    public PageList<CategoryResponse> getAllCategories(final Pageable pageable) {
        var categoryList = categoryRepository.findAllByStatusTrue(pageable);
        var categoryResponse = categoryList.getContent()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
        logger.info("Get all category with pagination successfully");
        return buildPaginatingResponse(categoryResponse, pageable.getPageSize(), pageable.getPageNumber(), categoryList.getTotalElements());

    }

    @Override
    public CategoryResponse createCategory(final CategoryRequest categoryRequest) {
        var category = categoryMapper.toCategoryEntity(categoryRequest);
        category.setStatus(Boolean.TRUE);
        category.setSlug(makeSlug(categoryRequest.getName()));
        var createdCategory = categoryRepository.save(category);
        logger.info("Create category successfully!");
        return categoryMapper.toCategoryResponse(createdCategory);

    }

    @Override
    public CategoryResponse updateCategory(final UUID id, final CategoryRequest categoryRequest) {
        var category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        category.setName(categoryRequest.getName());
        var updatedCategory = categoryRepository.save(category);
        logger.info("Update category successfully");
        return categoryMapper.toCategoryResponse(updatedCategory);

    }

    @Override
    @Transactional
    public void updateCategoryStatus(final UUID id, final Boolean status) {
        var category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        category.setStatus(status);
        categoryRepository.save(category);

        // Fetch all posts related to the category
        var posts = postRepository.findAllByCategoryIdAndStatusTrue(id);

        var updatedPosts = posts.stream()
                .peek(post -> post.setStatus(status))
                .toList();

        // Save updated posts
        postRepository.saveAll(updatedPosts);

        logger.info("Update category and related post statuses successfully");
    }

    @Override
    public CategoryResponse getCategoryByName(final String categoryName) {
        var category = categoryRepository
                .findByName(categoryName)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.info("Find By name {} category successfully", categoryName);
        return categoryMapper.toCategoryResponse(category);
    }

    private PageList<CategoryResponse> buildPaginatingResponse(final List<CategoryResponse> responses,
                                                               final int pageSize,
                                                               final int currentPage,
                                                               final long total) {
        return PageList.<CategoryResponse>builder()
                .records(responses)
                .limit(pageSize)
                .offset(currentPage)
                .totalRecords(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
    }
}
