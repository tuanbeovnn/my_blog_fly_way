package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.FollowType;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.enums.TopicType;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.*;
import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.repositories.*;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.request.TopicNotificationRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.UserFollowingResponse;
import com.myblogbackend.blog.response.UserLikedPostResponse;
import com.myblogbackend.blog.response.UserResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.myblogbackend.blog.utils.SlugUtil.makeSlug;

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
    private final NotificationService notificationService;
    private final FollowersRepository followersRepository;
    private final FirebaseUserRepository firebaseUserRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public PostResponse createPost(final PostRequest postRequest) throws ExecutionException, InterruptedException {
        var userEntity = usersRepository.findById(getSignedInUser().getId()).orElseThrow();
        var category = validateCategory(postRequest.getCategoryId());
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        postEntity.setStatus(Boolean.TRUE);
        postEntity.setApproved(Boolean.TRUE);
        postEntity.setFavourite(0L);
        postEntity.setSlug(makeSlug(postRequest.getTitle()));
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

        sendNotificationsToUser(userEntity, createdPost);

        logger.info("Post was created with id: {}", createdPost.getId());
        return postMapper.toPostResponse(createdPost);

    }

    private void sendNotificationsToUser(final UserEntity userEntity, final PostEntity createdPost)
            throws ExecutionException, InterruptedException {
        // Step 1: Get list of users who are following the user creating the post
        var followers = followersRepository.findByFollowedUserId(userEntity.getId());
        var usersFollowing = getUserFollowingResponses(followers);
        logger.info("User {} has {} followers.", userEntity.getId(), usersFollowing.size());
        if (!usersFollowing.isEmpty()) {
            // Step 2: Send notifications to each follower
            for (UserFollowingResponse follower : usersFollowing) {
                List<UserDeviceFireBaseTokenEntity> userDeviceFireBaseTokenEntities = firebaseUserRepository.findAllByUserId(follower.getId());
                if (!userDeviceFireBaseTokenEntities.isEmpty()) {
                    logger.info("Follower {} has {} device tokens.", follower.getId(), userDeviceFireBaseTokenEntities.size());

                    for (UserDeviceFireBaseTokenEntity deviceTokenEntity : userDeviceFireBaseTokenEntities) {
                        TopicNotificationRequest topicNotificationRequest = getTopicNotificationRequest(userEntity, createdPost, deviceTokenEntity);
                        notificationService.sendNotificationToDeviceWithSpecificTopic(topicNotificationRequest);
                        logger.info("Notification sent to device token: {}", deviceTokenEntity.getDeviceToken());
                    }
                } else {
                    logger.info("Follower {} has no device tokens.", follower.getId());
                }
            }
        } else {
            logger.info("User {} has no followers.", userEntity.getId());
        }
    }

    private TopicNotificationRequest getTopicNotificationRequest(final UserEntity userEntity,
                                                                 final PostEntity createdPost,
                                                                 final UserDeviceFireBaseTokenEntity deviceTokenEntity) {
        TopicNotificationRequest topicNotificationRequest = new TopicNotificationRequest();
        topicNotificationRequest.setTopicName(TopicType.NEWPOST);
        topicNotificationRequest.setDeviceToken(deviceTokenEntity.getDeviceToken());
        topicNotificationRequest.setTitle("New Post Notification");
        topicNotificationRequest.setBody(String.format("%s posted new post: %s", userEntity.getName().toUpperCase(), createdPost.getTitle()));
        Map<String, String> data = new HashMap<>();
        data.put("postId", String.valueOf(createdPost.getId()));
        topicNotificationRequest.setData(data);
        return topicNotificationRequest;
    }

    private @NotNull List<UserFollowingResponse> getUserFollowingResponses(final List<FollowersEntity> followers) {
        return followers.stream()
                .map(follower -> {
                    var followerUser = usersRepository.findById(follower.getFollower().getId()).orElseThrow();
                    return UserFollowingResponse.builder()
                            .id(followerUser.getId())
                            .name(followerUser.getName())
                            .build();
                })
                .toList();
    }

    @Override
    public PaginationPage<PostResponse> getAllPostByFilter(final Integer offset, final Integer limited, final PostFilterRequest filter) {
        var spec = PostSpec.filterBy(filter);
        var pageable = buildPageable(offset, limited, filter);
        var postEntities = postRepository.findAll(spec, pageable);

        UUID userId = getUserId();
        var postResponses = mapPostEntitiesToPostResponses(postEntities, userId);

        logger.info("Get feed list by filter succeeded with offset: {} and limited {}", offset, limited);
        return getPostResponsePaginationPage(offset, limited, postResponses, postEntities);
    }

    @Override
    public PaginationPage<PostResponse> getRelatedPosts(final Integer offset, final Integer limited, final PostFilterRequest filter, final UUID postId) {

        var spec = PostSpec.findRelatedArticles(filter, postId);
        var pageable = buildPageable(offset, limited, filter);
        var postEntities = postRepository.findAll(spec, pageable);
        UUID userId = getUserId();
        var postResponses = mapPostEntitiesToPostResponses(postEntities, userId);

        logger.info("Get related post list by filter succeeded with offset: {} and limited {}", offset, limited);
        return getPostResponsePaginationPage(offset, limited, postResponses, postEntities);
    }

    @Override
    @Transactional
    public void disablePost(final UUID postId) {
        // Fetch the post by ID and owner user ID
        var post = postRepository
                .findByIdAndUserId(postId, getSignedInUser().getId())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        logger.info("Disabling post successfully by id {}", postId);
        post.setStatus(false);
        postRepository.save(post);
        disableAllComments(postId);
    }

    private void disableAllComments(final UUID postId) {
        // Disable all comments following this post
        var comments = commentRepository.findByPostId(postId);
        comments.stream()
                .filter(comment -> comment.getPost().getId().equals(postId))
                .forEach(comment -> {
                    comment.setStatus(false);
                    commentRepository.save(comment);
                });
        logger.info("Disabled post and associated comments successfully");
    }


    @Override
    public PostResponse getPostById(final UUID id) {
        var userId = getUserId();
        var post = postRepository
                .findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.info("Get post successfully by id {} ", id);
        return getPostResponse(post, userId);
    }

    @Override
    public PostResponse getPostBySlug(final String slug) {
        var userId = getUserId();
        var post = postRepository
                .findBySlug(slug)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        logger.info("Get post successfully by slug {} ", slug);
        return getPostResponse(post, userId);
    }

    @Override
    @Transactional
    public PostResponse updatePost(final UUID id, final PostRequest postRequest) {
        var userId = getUserId();
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        var category = validateCategory(postRequest.getCategoryId());
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setThumbnails(GsonUtils.arrayToString(postRequest.getThumbnails()));
        post.setCategory(category);
        var updatedPost = postRepository.save(post);
        logger.info("Update post successfully with id {} ", id);
        return getPostResponse(post, userId);
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

    private UUID getUserId() {
        try {
            return getSignedInUser().getId();
        } catch (Exception e) {
            logger.info("No user logged in, proceeding without user context");
            return null;
        }
    }

    private Pageable buildPageable(final Integer offset, final Integer limited, final PostFilterRequest filter) {
        return PageRequest.of(
                offset,
                limited,
                Sort.Direction.fromString(filter.getSortDirection().toUpperCase()),
                filter.getSortField()
        );
    }

    private List<PostResponse> mapPostEntitiesToPostResponses(final Page<PostEntity> postEntities, final UUID userId) {
        return postEntities.getContent().stream()
                .map(postEntity -> getPostResponse(postEntity, userId))
                .toList();
    }

    @NotNull
    private PostResponse getPostResponse(final PostEntity post, final UUID userId) {
        var postResponse = postMapper.toPostResponse(post);
        postResponse.setUsersLikedPost(mapUserLikedPosts(post));
        postResponse.setFavoriteType(getUserFavoriteType(post, userId));
        var userResponse = getUserResponse(post.getUser(), userId);
        postResponse.setCreatedBy(userResponse);
        return postResponse;
    }

    private UserResponse getUserResponse(final UserEntity creator, final UUID userId) {
        if (creator == null) {
            return null;
        }
        var userResponse = userMapper.toUserDTO(creator);
        userResponse.setFollowType(getUserFollowingType(userId, creator.getId()));
        return userResponse;
    }

    private FollowType getUserFollowingType(final UUID followerId, final UUID followedUserId) {
        if (followerId == null || followedUserId == null) {
            return FollowType.UNFOLLOW;
        }
        return followersRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(FollowersEntity::getType)
                .orElse(FollowType.UNFOLLOW);
    }

    private List<UserLikedPostResponse> mapUserLikedPosts(final PostEntity post) {
        var favoriteEntities = favoriteRepository.findAllByPostId(post.getId());
        return favoriteEntities.stream()
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
    }

    private RatingType getUserFavoriteType(final PostEntity post, final UUID userId) {
        if (userId == null) {
            return RatingType.UNLIKE; // Default value for non-logged-in users
        }

        return favoriteRepository.findByUserIdAndPostId(userId, post.getId())
                .map(favoriteEntity -> RatingType.valueOf(favoriteEntity.getType().name()))
                .orElse(RatingType.UNLIKE);
    }

}
