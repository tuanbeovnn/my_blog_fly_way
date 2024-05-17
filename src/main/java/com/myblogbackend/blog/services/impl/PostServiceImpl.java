package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.pagination.OffsetPageRequest;
import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.utils.GsonUtils;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private static final Logger logger = LogManager.getLogger(PostServiceImpl.class);
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final PostMapper postMapper;

    @Override
    public PostResponse createPost(final PostRequest postRequest) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var category = validateCategory(postRequest.getCategoryId());
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        postEntity.setStatus(true);
        postEntity.setApproved(Boolean.TRUE);
        postEntity.setUser(usersRepository.findById(signedInUser.getId()).orElseThrow());
        // Validate and normalize tags
        var validFoodTags = validatePostTags(postRequest.getTags());
        // Convert FoodTag enum values to TagEntity instances
        var tagEntities = validFoodTags.stream()
                .map(tag -> TagEntity.builder().name(tag).build())
                .collect(Collectors.toSet());
        postEntity.setTags(tagEntities);
        var createdPost = postRepository.save(postEntity);
        logger.info("Post was created with id: {}", createdPost.getId());
        return postMapper.toPostResponse(createdPost);

    }

    private Set<PostTag> validatePostTags(final Set<PostTag> tags) {
        return tags.stream()
                .map(tag -> PostTag.fromString(tag.getType()))
                .collect(Collectors.toSet());
    }

    @Override
    public PaginationPage<PostResponse> getAllPostsByUserId(final Integer offset, final Integer limited) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var pageable = new OffsetPageRequest(offset, limited);
        var postEntities = postRepository.findAllByUserId(signedInUser.getId(), pageable);
        logger.info("Post get succeeded with offset: {} and limited {}", postEntities.getNumber(), postEntities.getSize());
        return getPostResponsePaginationPage(postEntities);

    }

    @Override
    public PaginationPage<PostResponse> getAllPostsByCategoryId(final Integer offset, final Integer limited, final UUID categoryId) {
        var pageable = new OffsetPageRequest(offset, limited);
        var posts = postRepository.findAllByCategoryId(pageable, categoryId);
        logger.info("Post get succeeded with offset: {} and limited {}", posts.getNumber(), posts.getSize());
        return getPostResponsePaginationPage(posts);

    }

    @Override
    public PostResponse getPostById(final UUID id) {
        var post = postRepository
                .findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.error("Get post successfully by id {} ", id);
        return postMapper.toPostResponse(post);

    }

    @Override
    public PostResponse updatePost(final UUID id, final PostRequest postRequest) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        var category = validateCategory(postRequest.getCategoryId());
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setImages(GsonUtils.arrayToString(postRequest.getImages()));
        post.setThumnails(GsonUtils.arrayToString(postRequest.getThumnails()));
        post.setCategory(category);
        var updatedPost = postRepository.save(post);
        logger.info("Update post successfully with id {} ", id);
        return postMapper.toPostResponse(updatedPost);
    }

    private CategoryEntity validateCategory(final UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
    }

    private PaginationPage<PostResponse> getPostResponsePaginationPage(final Page<PostEntity> postEntities) {
        var postResponses = postEntities.getContent().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
        return new PaginationPage<PostResponse>()
                .setRecords(postResponses)
                .setOffset(postEntities.getNumber())
                .setLimit(postEntities.getSize())
                .setTotalRecords(postEntities.getTotalElements());
    }
}
