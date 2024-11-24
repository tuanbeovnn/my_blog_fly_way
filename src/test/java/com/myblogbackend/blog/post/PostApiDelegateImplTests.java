package com.myblogbackend.blog.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.category.CategoryTestApi.makeCategoryForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static com.myblogbackend.blog.login.LoginTestApi.userPrincipal;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;
import static com.myblogbackend.blog.post.PostTestApi.preparePostForRequest;
import static com.myblogbackend.blog.post.PostTestApi.preparePostsEntitySaving;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PostApiDelegateImplTests {
    private static final String API_URL = "/api/v1/posts";
    private static final String PUBLIC_FEED_URL = "/api/v1/public/posts/feed";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private UsersRepository userRepository;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostMapper postMapper;

    @Test
    @Ignore
    public void givenValidPostData_whenCreatingNewPost_thenPostIsSuccessfullyCreatedAndReturnsExpectedDetails() throws Exception {
        // Mock JWTSecurityUtil
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito.mockStatic(JWTSecurityUtil.class)) {
            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo).thenReturn(Optional.of(userPrincipal()));

            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(makeCategoryForSaving("Category A")));

            PostEntity savedPostEntity = makePostForSaving("Title A", "Description A");
            when(postRepository.save(any(PostEntity.class))).thenReturn(savedPostEntity);

            var expectedPostResponse = postMapper.toPostResponse(savedPostEntity);

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                            .content(objectMapper.writeValueAsString(preparePostForRequest()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.id").value(expectedPostResponse.getId().toString()))
                    .andExpect(jsonPath("$.details.title").value(expectedPostResponse.getTitle()));
        }
    }

    @Test
    public void givenEmptyShortDescription_whenCreatingNewPost_thenReturnsBadRequestWithValidationErrorMessage() throws Exception {
        var postRequest = preparePostForRequest();
        postRequest.setShortDescription("");

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                        .content(objectMapper.writeValueAsString(postRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0].message").value("Short Description info cannot be blank"));
    }

    @Test
    public void givenUserRequestForListPost_whenRequestListPost_thenReturnsListPost() throws Exception {
        // Prepare a list of post entities and specify total records
        var postEntityList = preparePostsEntitySaving(); // This should return a list with the posts you want to test
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
                .andExpect(jsonPath("$.details.records[0].title", is(expectedPostList.get(0).getTitle())))
                .andExpect(jsonPath("$.details.records[1].title", is(expectedPostList.get(1).getTitle())));
    }
}