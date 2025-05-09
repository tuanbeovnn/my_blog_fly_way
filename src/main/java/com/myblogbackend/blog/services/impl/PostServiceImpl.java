package com.myblogbackend.blog.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.config.security.UserPrincipal;
import com.myblogbackend.blog.enums.FollowType;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.PostType;
import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.event.dto.NotificationEvent;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.mapper.UserMapper;
import com.myblogbackend.blog.models.*;
import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.repositories.*;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.UserFollowingResponse;
import com.myblogbackend.blog.response.UserLikedPostResponse;
import com.myblogbackend.blog.response.UserResponse;
import com.myblogbackend.blog.response.UserResponse.ProfileResponseDTO;
import com.myblogbackend.blog.response.UserResponse.SocialLinksDTO;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.specification.PostSpec;
import com.myblogbackend.blog.utils.GsonUtils;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.myblogbackend.blog.enums.NotificationType.NEW_POST;
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
    private final FollowersRepository followersRepository;
    private final FirebaseUserRepository firebaseUserRepository;
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration DRAFT_EXPIRATION = Duration.ofHours(12);

    @Override
    @Transactional
    public PostResponse createPost(final PostRequest postRequest) throws ExecutionException, InterruptedException {
        var userEntity = usersRepository.findById(getUserId()).orElseThrow();
        var category = validateCategory(postRequest.getCategoryId());
        var postEntity = postMapper.toPostEntity(postRequest);
        postEntity.setCategory(category);
        postEntity.setStatus(Boolean.TRUE);
        postEntity.setPostType(PostType.PENDING);
        postEntity.setFavourite(0L);
        postEntity.setSlug(makeSlug(postRequest.getTitle()));
        postEntity.setCreatedBy(userEntity.getName());
        postEntity.setUser(userEntity);

        var tagEntities = validatePostTags(postRequest.getTags()).stream()
                .map(tag -> TagEntity.builder().name(tag).build())
                .collect(Collectors.toSet());
        postEntity.setTags(tagEntities);

        var createdPost = postRepository.save(postEntity);
        logger.info("Post was created with id: {}", createdPost.getId());

        // Remove the draft from Redis after successful publishing
        redisTemplate.delete(getDraftRedisKey(userEntity.getEmail()));
        sendAMessageFromKafkaToNotificationService(userEntity, createdPost);
        return buildPostResponse(createdPost, userEntity.getId());

    }

    public PostResponse saveDraft(final PostRequest postRequest) {
        var userEntity = usersRepository.findById(getUserId()).orElseThrow();
        var draftKey = getDraftRedisKey(userEntity.getEmail());
        var postRequestJson = GsonUtils.objectToString(postRequest);
        var postResponse = GsonUtils.stringToObject(postRequestJson, PostResponse.class);
        redisTemplate.opsForValue().set(draftKey, postRequestJson, DRAFT_EXPIRATION);
        logger.info("Draft saved for user: {}", userEntity.getEmail());
        return postResponse;
    }

    public PostRequest getSavedDraft() {
        var userEntity = usersRepository.findById(getUserId()).orElseThrow();
        var draftKey = getDraftRedisKey(userEntity.getEmail());

        var postRequestJson = redisTemplate.opsForValue().get(draftKey);
        if (postRequestJson != null) {
            try {
                return objectMapper.readValue(postRequestJson, PostRequest.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize draft from Redis", e);
            }
        }
        return new PostRequest();
    }

    // Helper method to generate Redis key for draft
    private String getDraftRedisKey(final String userEmail) {
        return "draft:" + userEmail;
    }

    private void sendAMessageFromKafkaToNotificationService(final UserEntity userEntity, final PostEntity createdPost)
            throws ExecutionException, InterruptedException {

        var followers = followersRepository.findByFollowedUserId(userEntity.getId());
        var usersFollowing = getUserFollowingResponses(followers);
        logger.info("User {} has {} followers.", userEntity.getId(), usersFollowing.size());

        if (usersFollowing.isEmpty()) {
            logger.info("User {} has no followers.", userEntity.getId());
            return;
        }

        Map<String, Set<UUID>> tokenToUserMap = buildTokenToUserMap(usersFollowing);
        sendNotifications(tokenToUserMap, createdPost, userEntity);
    }

    private Map<String, Set<UUID>> buildTokenToUserMap(final List<UserFollowingResponse> usersFollowing) {
        Map<String, Set<UUID>> tokenToUserMap = new HashMap<>();

        for (UserFollowingResponse follower : usersFollowing) {
            List<UserDeviceFireBaseTokenEntity> userDeviceTokens = firebaseUserRepository.findAllByUserId(follower.getId());
            for (UserDeviceFireBaseTokenEntity deviceTokenEntity : userDeviceTokens) {
                String token = deviceTokenEntity.getDeviceToken();
                tokenToUserMap.computeIfAbsent(token, k -> new HashSet<>()).add(follower.getId());
            }
        }

        return tokenToUserMap;
    }

    private void sendNotifications(final Map<String, Set<UUID>> tokenToUserMap, final PostEntity createdPost, final UserEntity userEntity) {
        for (Map.Entry<String, Set<UUID>> entry : tokenToUserMap.entrySet()) {
            String token = entry.getKey();
            Set<UUID> userIds = entry.getValue();
            NotificationEvent notificationEvent = createNotificationEvent(token, userIds, createdPost, userEntity);
            sendNotificationEvent(notificationEvent, token);
        }
    }

    private NotificationEvent createNotificationEvent(final String token, final Set<UUID> userIds, final PostEntity createdPost, final UserEntity userEntity) {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setDeviceTokenId(token);
        notificationEvent.setNotificationType(String.valueOf(NEW_POST));

        Map<String, String> data = new HashMap<>();
        data.put("postId", String.valueOf(createdPost.getId()));
        data.put("userIds", String.valueOf(new ArrayList<>(userIds)));
        data.put("postSlug", createdPost.getSlug());
        data.put("postTitle", createdPost.getTitle());
        data.put("userName", userEntity.getUserName());
        data.put("userEmail", userEntity.getEmail());
        data.put("name", userEntity.getName());
        data.put("createdDate", String.valueOf(createdPost.getCreatedDate()));
        String avatarUrl = (userEntity.getProfile() != null && userEntity.getProfile().getAvatarUrl() != null)
                ? userEntity.getProfile().getAvatarUrl()
                : "";
        data.put("userAvatar", avatarUrl);
        notificationEvent.setData(data);

        return notificationEvent;
    }

    private void sendNotificationEvent(final NotificationEvent notificationEvent, final String token) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("notification-topic", notificationEvent);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Error sending notification event to token {}: ", token, ex);
                    // Optionally: add error handling like saving the failure to the database.
                } else {
                    logger.info("Notification event sent to token {} successfully.", token);
                }
            });
        } catch (Exception e) {
            logger.error("Exception occurred while sending notification event to token {}: ", token, e);
        }
    }

    private @NotNull List<UserFollowingResponse> getUserFollowingResponses(final List<FollowersEntity> followers) {
        return followers.stream()
                .map(follower -> {
                    var followerUser = usersRepository.findById(follower.getFollower().getId()).orElseThrow();
                    return UserFollowingResponse.builder()
                            .id(followerUser.getId())
                            .name(followerUser.getName())
                            .userName(followerUser.getUserName())
                            .build();
                })
                .toList();
    }

    @Override
    public PageList<PostResponse> getAllPostByFilter(final Pageable pageable, final PostFilterRequest filter) {
        var spec = PostSpec.filterBy(filter);
        var pageableBuild = buildPageable(pageable, filter);
        var postEntities = postRepository.findAll(spec, pageableBuild);

        return buildPaginatedPostResponse(postEntities, pageable.getPageSize(), pageable.getPageNumber());
    }

    @Override
    public PageList<PostResponse> getAllPostByStatus(final Pageable pageable, final PostFilterRequest filter) {
        var spec = PostSpec.findAllArticles(filter);
        var pageableBuild = buildPageable(pageable, filter);
        var postEntities = postRepository.findAll(spec, pageableBuild);

        return buildPaginatedPostResponse(postEntities, pageable.getPageSize(), pageable.getPageNumber());
    }

    @Override
    public PostResponse approvePost(final UUID id, final PostType postType) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        post.setPostType(postType);
        var updatedPost = postRepository.save(post);

        // Send a notification based on postType (APPROVED or REJECTED)

        return buildPostResponse(updatedPost, getUserId());
    }

    @Override
    public PageList<PostResponse> relatedPosts(final Pageable pageable, final PostFilterRequest filter) {
        var spec = PostSpec.findRelatedArticles(filter);
        var pageableBuild = buildPageable(pageable, filter);
        var postEntities = postRepository.findAll(spec, pageableBuild);

        return buildPaginatedPostResponse(postEntities, pageable.getPageSize(), pageable.getPageNumber());
    }

    @Override
    @Transactional
    public void disablePost(final UUID postId) {
        var post = postRepository.findByIdAndUserId(postId, getUserId())
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        post.setStatus(false);
        postRepository.save(post);
        disableAllComments(postId);
        logger.info("Post disabled successfully by id {}", postId);
    }

    private void disableAllComments(final UUID postId) {
        var comments = commentRepository.findByPostId(postId);
        comments.forEach(comment -> {
            comment.setStatus(false);
            commentRepository.save(comment);
        });
        logger.info("Disabled all comments for post {}", postId);
    }

    @Override
    public PostResponse getPostBySlug(final String slug) {
        var post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
        return buildPostResponse(post, getUserId());
    }

    @Override
    @Transactional
    public PostResponse updatePost(final UUID id, final PostRequest postRequest) {
        var post = postRepository.findById(id)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        var currentUserId = getUserId();

        if (!post.getUser().getId().equals(currentUserId)) {
            throw new BlogRuntimeException(ErrorCode.USER_NOT_ALLOWED);
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setThumbnails(GsonUtils.arrayToString(postRequest.getThumbnails()));
        post.setPostType(PostType.PENDING);

        var updatedPost = postRepository.save(post);
        logger.info("Post updated successfully with id {}", id);
        return buildPostResponse(updatedPost, getUserId());
    }

    // Utility Methods

    private PostResponse buildPostResponse(final PostEntity post, final UUID userId) {
        var postResponse = postMapper.toPostResponse(post);
        postResponse.setUsersLikedPost(mapUserLikedPosts(post));
        postResponse.setFavoriteType(getUserFavoriteType(post, userId));
        postResponse.setTotalComments(countCommentByPostId(post));
        postResponse.setCreatedBy(getUserResponse(post.getUser(), userId));
        return postResponse;
    }

    private PageList<PostResponse> buildPaginatedPostResponse(final Page<PostEntity> postEntities, final int pageSize, final int currentPage) {
        var postResponses = postEntities.getContent().stream()
                .map(post -> buildPostResponse(post, getUserId()))
                .toList();
        return buildPaginatingResponse(postResponses, pageSize, currentPage, postEntities.getTotalElements());
    }

    private CategoryEntity validateCategory(final UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));
    }

    private Set<PostTag> validatePostTags(final Set<PostTag> tags) {
        return tags.stream()
                .map(tag -> PostTag.fromString(tag.getType()))
                .collect(Collectors.toSet());
    }

    private UUID getUserId() {
        return getSignedInUser().map(UserPrincipal::getId).orElse(null);
    }

    private static Optional<UserPrincipal> getSignedInUser() {
        return JWTSecurityUtil.getJWTUserInfo();
    }

    private int countCommentByPostId(final PostEntity post) {
        return commentRepository.countByPostIdAndStatusTrueOrderByCreatedDateDesc(post.getId());
    }

    private UserResponse getUserResponse(final UserEntity user, final UUID userId) {
        if (user == null) return null;
        var userResponse = userMapper.toUserDTO(user);
        userResponse.setFollowType(getUserFollowingType(userId, user.getId()));
        return enrichUserProfile(user, userResponse);
    }

    private UserResponse enrichUserProfile(final UserEntity user, final UserResponse response) {
        var profile = user.getProfile();
        if (profile == null) return response;

        var profileResponse = ProfileResponseDTO.builder()
                .bio(profile.getBio())
                .website(profile.getWebsite())
                .location(profile.getLocation())
                .avatarUrl(profile.getAvatarUrl())
                .social(buildSocialLinks(profile))
                .build();
        response.setProfile(profileResponse);
        return response;
    }

    private SocialLinksDTO buildSocialLinks(final ProfileEntity profile) {
        return SocialLinksDTO.builder()
                .twitter(profile.getSocial().getTwitter())
                .linkedin(profile.getSocial().getLinkedin())
                .github(profile.getSocial().getGithub())
                .build();
    }

    private FollowType getUserFollowingType(final UUID followerId, final UUID followedUserId) {
        if (followerId == null || followedUserId == null) return FollowType.UNFOLLOW;
        return followersRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(FollowersEntity::getType)
                .orElse(FollowType.UNFOLLOW);
    }

    private List<UserLikedPostResponse> mapUserLikedPosts(final PostEntity post) {
        return favoriteRepository.findAllByPostId(post.getId()).stream()
                .map(fav -> UserLikedPostResponse.builder()
                        .id(fav.getUser().getId())
                        .name(fav.getUser().getName())
                        .type(Optional.ofNullable(fav.getType())
                                .orElse(RatingType.UNLIKE))
                        .build())
                .toList();
    }

    private RatingType getUserFavoriteType(final PostEntity post, final UUID userId) {
        return favoriteRepository.findByUserIdAndPostId(userId, post.getId())
                .map(fav -> RatingType.valueOf(fav.getType().name()))
                .orElse(RatingType.UNLIKE);
    }

    private Pageable buildPageable(final Pageable pageable, final PostFilterRequest filter) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.Direction.fromString(filter.getSortDirection().toUpperCase()), filter.getSortField());
    }

    private PageList<PostResponse> buildPaginatingResponse(final List<PostResponse> responses, final int pageSize, final int currentPage, final long total) {
        return PageList.<PostResponse>builder()
                .records(responses)
                .limit(pageSize)
                .offset(currentPage)
                .totalRecords(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
    }

}
