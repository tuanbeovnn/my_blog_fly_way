package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.FavoriteEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.pagination.OffsetPageRequest;
import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.FavoriteRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.UserLikedPostResponse;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.specification.PostSpec;
import com.myblogbackend.blog.utils.GsonUtils;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final FavoriteRepository favoriteRepository;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public PostResponse createPost(final PostRequest postRequest) {
        var userEntity = usersRepository.findById(getSignedInUser().getId()).orElseThrow();
        var category = validateCategory(postRequest.getCategoryId());
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        postEntity.setStatus(Boolean.TRUE);
        postEntity.setApproved(Boolean.TRUE);
        postEntity.setFavourite(0L);
        postEntity.setCreatedBy(getSignedInUser().getName());
        postEntity.setUser(usersRepository.findById(userEntity.getId()).orElseThrow());
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

    @Override
    public PaginationPage<PostResponse> getAllPostsByUserId(final UUID userId, final Integer offset, final Integer limited) {

        var pageable = new OffsetPageRequest(offset, limited);
        var postEntities = postRepository.findAllByUserIdAndStatusTrueOrderByCreatedDateDesc(userId, pageable);

        var postResponses = getPostResponses(postEntities, userId);

        logger.info("Post get succeeded with offset: {} and limited {}", postEntities.getNumber(), postEntities.getSize());
        return getPostResponsePaginationPage(offset, limited, postResponses, postEntities);
    }

    @Override
    public PaginationPage<PostResponse> getAllPostByFilter(final Integer offset, final Integer limited, final PostFilterRequest filter) {
        // Call the static method directly from the class/interface
        var spec = PostSpec.filterBy(filter);
        var pageable = PageRequest.of(offset, limited,
                Sort.Direction.fromString(filter.getSortDirection().toUpperCase()),
                filter.getSortField());
        var postEntities = postRepository.findAll(spec, pageable);

        UUID userId = null;
        try {
            userId = getSignedInUser().getId(); // Try to get the signed-in user
        } catch (Exception e) {
            logger.info("No user logged in, proceeding without user context");
        }

        var postResponses = getPostResponses(postEntities, userId);

        logger.info("Get feed list by filter succeeded with offset: {} and limited {}", offset, limited);
        return getPostResponsePaginationPage(offset, limited, postResponses, postEntities);
    }

    @NotNull
    private List<PostResponse> getPostResponses(final Page<PostEntity> postEntities, final UUID userId) {
        return postEntities.getContent().stream()
                .map(postEntity -> getPostResponse(postEntity, userId))
                .toList();
    }

    @NotNull
    private PostResponse getPostResponse(final PostEntity post, final UUID userId) {
        var postResponse = postMapper.toPostResponse(post);
        // Set users who liked the post
        var favoriteEntities = favoriteRepository.findAllByPostId(post.getId());
        var userLikedPosts = favoriteEntities.stream()
                .map(favoriteEntity -> {
                    var userResponse = userMapper.toUserResponse(favoriteEntity.getUser());
                    var type = favoriteEntity.getType() == null ? RatingType.UNLIKE : RatingType.valueOf(favoriteEntity.getType().name());
                    return UserLikedPostResponse.builder()
                            .id(userResponse.getId())
                            .name(userResponse.getName())
                            .type(type)
                            .build();
                })
                .toList();
        postResponse.setUsersLikedPost(userLikedPosts);

        // Set favorite type for the signed-in user
        if (userId != null) {
            var favoriteEntityOpt = favoriteRepository.findByUserIdAndPostId(userId, post.getId());
            var ratingType = favoriteEntityOpt
                    .map(FavoriteEntity::getType)
                    .map(type -> RatingType.valueOf(type.name()))
                    .orElse(RatingType.UNLIKE);
            postResponse.setFavoriteType(ratingType);
        } else {
            postResponse.setFavoriteType(RatingType.UNLIKE); // Default value for non-logged-in users
        }
        return postResponse;
    }

    @Override
    public PostResponse getPostById(final UUID id) {
        var post = postRepository
                .findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.info("Get post successfully by id {} ", id);
        return postMapper.toPostResponse(post);

    }

    @Override
    @Transactional
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

    private static PaginationPage<PostResponse> getPostResponsePaginationPage(final Integer offset,
                                                                              final Integer limited,
                                                                              final List<PostResponse> postResponses,
                                                                              final Page<PostEntity> postEntities) {
        return new PaginationPage<PostResponse>()
                .setRecords(postResponses)
                .setOffset(offset)
                .setLimit(limited)
                .setTotalRecords(postEntities.getTotalElements());
    }

    @NotNull
    private static UserPrincipal getSignedInUser() {
        return JWTSecurityUtil.getJWTUserInfo().orElseThrow();
    }

    private Set<PostTag> validatePostTags(final Set<PostTag> tags) {
        return tags.stream()
                .map(tag -> PostTag.fromString(tag.getType()))
                .collect(Collectors.toSet());
    }

}
