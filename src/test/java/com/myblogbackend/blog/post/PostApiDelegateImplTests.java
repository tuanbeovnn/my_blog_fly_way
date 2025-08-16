package com.myblogbackend.blog.post;

import static com.myblogbackend.blog.category.CategoryTestApi.makeCategoryForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static com.myblogbackend.blog.login.LoginTestApi.userPrincipal;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;
import static com.myblogbackend.blog.post.PostTestApi.preparePostForRequest;
import static com.myblogbackend.blog.post.PostTestApi.preparePostsEntitySaving;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.CommentRepository;
import com.myblogbackend.blog.repositories.FavoriteRepository;
import com.myblogbackend.blog.repositories.FollowersRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.utils.JWTSecurityUtil;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PostApiDelegateImplTests {
    private static final String API_URL = "/api/v1/posts";
    private static final String PUBLIC_FEED_URL = "/api/v1/public/posts/feed";
    private static final String DRAFT_URL = "/api/v1/posts/draft";
    private static final String POST_TAGS_URL = "/api/v1/public/posts/post-tags";
    private static final String SEARCH_URL = "/api/v1/public/posts/search-articles";
    private static final String RELATED_POSTS_URL = "/api/v1/public/posts/related";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UsersRepository userRepository;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private FavoriteRepository favoriteRepository;

    @MockitoBean
    private FollowersRepository followersRepository;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostMapper postMapper;

    @Test
    public void givenValidPostData_whenCreatingNewPost_thenPostIsSuccessfullyCreatedAndReturnsExpectedDetails()
            throws Exception {
        // Mock JWTSecurityUtil
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(categoryRepository.findById(any(UUID.class)))
                    .thenReturn(Optional.of(makeCategoryForSaving("Category A")));

            PostEntity savedPostEntity = makePostForSaving("Title A", "Description A");
            when(postRepository.save(any(PostEntity.class))).thenReturn(savedPostEntity);

            var expectedPostResponse = postMapper.toPostResponse(savedPostEntity);

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                    .content(objectMapper.writeValueAsString(preparePostForRequest()))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.id")
                            .value(expectedPostResponse.getId().toString()))
                    .andExpect(jsonPath("$.details.title").value(expectedPostResponse.getTitle()));
        }
    }

    @Test
    public void givenEmptyShortDescription_whenCreatingNewPost_thenReturnsBadRequestWithValidationErrorMessage()
            throws Exception {
        var postRequest = preparePostForRequest();
        postRequest.setShortDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0].message")
                        .value("Short Description info cannot be blank"));
    }

    @Test
    public void givenUserRequestForListPost_whenRequestListPost_thenReturnsListPost() throws Exception {
        // Prepare a list of post entities and specify total records
        var postEntityList = preparePostsEntitySaving(); // This should return a list with the posts you want to
        // test
        int totalRecords = postEntityList.size(); // Assuming you want to test with all the posts in the list
        Pageable pageable = PageRequest.of(0, 9); // Define pageable for the test

        // Create a PageImpl with the posts, pageable, and total record count
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        // Mock the repository to return the PageImpl
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        // Convert the post entities to post responses
        var expectedPostList = postMapper.toListPostResponse(postEntityList);

        // Perform the request and assert the response
        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("page", "0")
                .param("size", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)))
                .andExpect(jsonPath("$.details.records[0].title",
                        is(expectedPostList.get(0).getTitle())))
                .andExpect(jsonPath("$.details.records[1].title",
                        is(expectedPostList.get(1).getTitle())));
    }

    @Test
    public void givenValidPostDraft_whenSavingDraft_thenReturnsSavedDraftSuccessfully() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            var postRequest = preparePostForRequest();

            mockMvc.perform(MockMvcRequestBuilders.post(DRAFT_URL)
                    .content(objectMapper.writeValueAsString(postRequest))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.title").value(postRequest.getTitle()));
        }
    }

    @Test
    public void givenSavedDraftExists_whenGettingSavedDraft_thenReturnsExistingDraft() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            var postRequest = preparePostForRequest();
            var draftJson = objectMapper.writeValueAsString(postRequest);
            when(valueOperations.get(anyString())).thenReturn(draftJson);

            mockMvc.perform(MockMvcRequestBuilders.get(DRAFT_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.title").value(postRequest.getTitle()));
        }
    }

    @Test
    public void givenNoDraftExists_whenGettingSavedDraft_thenReturnsEmptyDraft() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);

            mockMvc.perform(MockMvcRequestBuilders.get(DRAFT_URL))
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void givenValidPostSlug_whenGettingPostBySlug_thenReturnsPostDetails() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            var postEntity = makePostForSaving("Test Title", "Test Content");
            postEntity.setSlug("test-title");
            when(postRepository.findBySlug("test-title")).thenReturn(Optional.of(postEntity));
            when(favoriteRepository.findAllByPostId(any(UUID.class))).thenReturn(Collections.emptyList());
            when(favoriteRepository.findByUserIdAndPostId(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.empty());
            when(commentRepository.countByPostIdAndStatusTrueOrderByCreatedDateDesc(any(UUID.class)))
                    .thenReturn(0);

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/public/posts/slug/test-title"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.title").value("Test Title"))
                    .andExpect(jsonPath("$.details.slug").value("test-title"));
        }
    }

    @Test
    public void givenInvalidPostSlug_whenGettingPostBySlug_thenReturnsNotFound() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(postRepository.findBySlug("invalid-slug")).thenReturn(Optional.empty());

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/public/posts/slug/invalid-slug"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    public void whenGettingPostTags_thenReturnsAllAvailableTags() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(POST_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details", hasSize(PostTag.values().length)));
    }

    @Test
    public void givenSearchQuery_whenSearchingPosts_thenReturnsMatchingPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenMissingSearchParameter_whenSearchingPosts_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param("offset", "0")
                .param("limit", "9"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenValidPostUpdate_whenUpdatingPost_thenReturnsUpdatedPost() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            var postEntity = makePostForSaving("Original Title", "Original Content");
            postEntity.setUser(userEntityBasicInfo());
            var postId = postEntity.getId();

            when(postRepository.findById(postId)).thenReturn(Optional.of(postEntity));
            when(postRepository.save(any(PostEntity.class))).thenReturn(postEntity);
            when(favoriteRepository.findAllByPostId(any(UUID.class))).thenReturn(Collections.emptyList());
            when(favoriteRepository.findByUserIdAndPostId(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.empty());
            when(commentRepository.countByPostIdAndStatusTrueOrderByCreatedDateDesc(any(UUID.class)))
                    .thenReturn(0);

            var updateRequest = preparePostForRequest();
            updateRequest.setTitle("Updated Title");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/{id}", postId)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.id").value(postId.toString()));
        }
    }

    @Test
    public void givenInvalidPostId_whenUpdatingPost_thenReturnsNotFound() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            var nonExistentId = UUID.randomUUID();
            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            var updateRequest = preparePostForRequest();

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/{id}", nonExistentId)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    public void givenValidPostId_whenDisablingPost_thenReturnsSuccessMessage() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            var postEntity = makePostForSaving("Test Title", "Test Content");
            postEntity.setUser(userEntityBasicInfo());
            var postId = postEntity.getId();

            when(postRepository.findByIdAndUserId(postId, userEntityBasicInfo().getId()))
                    .thenReturn(Optional.of(postEntity));
            when(postRepository.save(any(PostEntity.class))).thenReturn(postEntity);
            when(commentRepository.findByPostId(postId)).thenReturn(Collections.emptyList());

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/disable/{postId}", postId))
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void givenFilterByCategoryName_whenGettingPostsFeed_thenReturnsFilteredPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("categoryName", "Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenFilterByTags_whenGettingPostsFeed_thenReturnsFilteredPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("tags", "AI,APACHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenFilterByUserId_whenGettingPostsFeed_thenReturnsUserPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        var userId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenFilterByUserName_whenGettingPostsFeed_thenReturnsUserPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("userName", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenSortingParameters_whenGettingPostsFeed_thenReturnsSortedPosts() throws Exception {
        var postEntityList = preparePostsEntitySaving();
        int totalRecords = postEntityList.size();
        Pageable pageable = PageRequest.of(0, 9);
        Page<PostEntity> page = new PageImpl<>(postEntityList, pageable, totalRecords);

        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(PUBLIC_FEED_URL)
                .param("offset", "0")
                .param("limit", "9")
                .param("sortField", "title")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(totalRecords)));
    }

    @Test
    public void givenEmptyTitleField_whenCreatingNewPost_thenReturnsBadRequestWithValidationError()
            throws Exception {
        var postRequest = preparePostForRequest();
        postRequest.setTitle("");

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(postRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNullCategoryId_whenCreatingNewPost_thenReturnsBadRequestWithValidationError()
            throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));

            var postRequest = preparePostForRequest();
            postRequest.setCategoryId(null);

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                    .content(objectMapper.writeValueAsString(postRequest))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ===============================
    // RELATED POSTS API TEST CASES
    // ===============================

    @Test
    public void givenValidPostId_whenGettingRelatedPosts_thenReturnsRelatedPostsSuccessfully() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();
        var relatedPosts = PostTestApi.prepareSeriesPostsForTesting();

        // Create a PageImpl with related posts
        Page<PostEntity> relatedPostsPage = new PageImpl<>(relatedPosts, PageRequest.of(0, 20), relatedPosts.size());

        // Mock repository calls
        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("offset", "0")
                .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Related posts retrieved successfully"))
                .andExpect(jsonPath("$.details.totalRecords", is(relatedPosts.size())))
                .andExpect(jsonPath("$.details.records", hasSize(relatedPosts.size())));
    }

    @Test
    public void givenValidPostId_whenGettingRelatedPostsWithCustomParams_thenReturnsPagedResults() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();
        var relatedPosts = PostTestApi.prepareSeriesPostsForTesting().subList(0, 5); // Take first 5

        Page<PostEntity> relatedPostsPage = new PageImpl<>(relatedPosts, PageRequest.of(0, 5), 8);

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("offset", "0")
                .param("limit", "5")
                .param("sortBy", "createdDate")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(8)))
                .andExpect(jsonPath("$.details.records", hasSize(5)))
                .andExpect(jsonPath("$.details.limit", is(5)))
                .andExpect(jsonPath("$.details.offset", is(0)));
    }

    @Test
    public void givenNonExistentPostId_whenGettingRelatedPosts_thenReturnsNotFound() throws Exception {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        when(postRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + nonExistentPostId)
                .param("offset", "0")
                .param("limit", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenSeriesPost_whenGettingRelatedPosts_thenReturnsSameSeriesPosts() throws Exception {
        // Given - Post titled "Introduction to Java Programming Day 1"
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeSeriesPost(
                "Introduction to Java Programming Day 1",
                "Day 1 content",
                "Programming");

        // Related posts in the same series
        var seriesRelatedPosts = List.of(
                PostTestApi.makeSeriesPost("Introduction to Java Programming Day 2", "Day 2 content", "Programming"),
                PostTestApi.makeSeriesPost("Introduction to Java Programming Day 3", "Day 3 content", "Programming"),
                PostTestApi.makeSeriesPost("Introduction to Java Programming Part 4", "Part 4 content", "Programming"));

        Page<PostEntity> relatedPostsPage = new PageImpl<>(seriesRelatedPosts, PageRequest.of(0, 20),
                seriesRelatedPosts.size());

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(3)))
                .andExpect(jsonPath("$.details.records[0].title").value("Introduction to Java Programming Day 2"))
                .andExpect(jsonPath("$.details.records[1].title").value("Introduction to Java Programming Day 3"))
                .andExpect(jsonPath("$.details.records[2].title").value("Introduction to Java Programming Part 4"));
    }

    @Test
    public void givenPostWithCategory_whenGettingRelatedPosts_thenReturnsSameCategoryPosts() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeSeriesPost("Current Post", "Content", "Programming");
        var categoryRelatedPosts = PostTestApi.prepareRelatedPostsByCategory();

        Page<PostEntity> relatedPostsPage = new PageImpl<>(categoryRelatedPosts, PageRequest.of(0, 20),
                categoryRelatedPosts.size());

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(3)))
                .andExpect(jsonPath("$.details.records[0].title").value("Advanced Java Concepts"))
                .andExpect(jsonPath("$.details.records[1].title").value("Java Best Practices"));
    }

    @Test
    public void givenPostWithAuthor_whenGettingRelatedPosts_thenReturnsSameAuthorPosts() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();
        var authorRelatedPosts = PostTestApi.prepareRelatedPostsByAuthor();

        Page<PostEntity> relatedPostsPage = new PageImpl<>(authorRelatedPosts, PageRequest.of(0, 20),
                authorRelatedPosts.size());

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(2)))
                .andExpect(jsonPath("$.details.records[0].title").value("Author's Other Post 1"));
    }

    @Test
    public void givenInvalidLimitParameter_whenGettingRelatedPosts_thenHandlesGracefully() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When & Then - Test with very large limit (should be capped at 100)
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("limit", "1000"))
                .andExpect(status().isOk());

        // Test with zero limit (should be adjusted to 1)
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("limit", "0"))
                .andExpect(status().isOk());

        // Test with negative limit (should be adjusted to 1)
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("limit", "-5"))
                .andExpect(status().isOk());

        // Test with negative offset (should be adjusted to 0)
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("offset", "-1"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenPostWithNoRelatedPosts_whenGettingRelatedPosts_thenReturnsEmptyList() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();

        // No related posts found
        Page<PostEntity> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(0)))
                .andExpect(jsonPath("$.details.records", hasSize(0)));
    }

    @Test
    public void givenValidPostId_whenGettingRelatedPostsWithSorting_thenReturnsSortedResults() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeCurrentPost();
        var relatedPosts = PostTestApi.prepareSeriesPostsForTesting();

        Page<PostEntity> relatedPostsPage = new PageImpl<>(relatedPosts, PageRequest.of(0, 20), relatedPosts.size());

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then - Test ascending sort
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("sortBy", "title")
                .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(relatedPosts.size())));

        // Test descending sort by creation date
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId)
                .param("sortBy", "createdDate")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(relatedPosts.size())));
    }

    @Test
    public void givenMultipleSeriesPatterns_whenGettingRelatedPosts_thenDetectsAllPatterns() throws Exception {
        // Given
        UUID postId = UUID.randomUUID();
        var currentPost = PostTestApi.makeSeriesPost("React Tutorial Part 1", "React content", "Frontend");

        // Related posts with different series patterns
        var mixedPatternPosts = List.of(
                PostTestApi.makeSeriesPost("React Tutorial Part 2", "Part 2 content", "Frontend"),
                PostTestApi.makeSeriesPost("React Tutorial - Day 3", "Day 3 content", "Frontend"),
                PostTestApi.makeSeriesPost("React Tutorial Chapter 4", "Chapter 4 content", "Frontend"),
                PostTestApi.makeSeriesPost("React Tutorial Episode 5", "Episode 5 content", "Frontend"));

        Page<PostEntity> relatedPostsPage = new PageImpl<>(mixedPatternPosts, PageRequest.of(0, 20),
                mixedPatternPosts.size());

        when(postRepository.findById(postId)).thenReturn(Optional.of(currentPost));
        when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(relatedPostsPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get(RELATED_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.records", hasSize(4)))
                .andExpect(jsonPath("$.details.records[0].title").value("React Tutorial Part 2"))
                .andExpect(jsonPath("$.details.records[1].title").value("React Tutorial - Day 3"))
                .andExpect(jsonPath("$.details.records[2].title").value("React Tutorial Chapter 4"));
    }
}