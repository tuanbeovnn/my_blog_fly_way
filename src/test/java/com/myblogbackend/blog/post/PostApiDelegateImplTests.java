package com.myblogbackend.blog.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.mapper.PostMapper;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PostApiDelegateImplTests {

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

    @Before
    public void setUp() {
        Mockito.mockStatic(JWTSecurityUtil.class);
        Mockito.when(JWTSecurityUtil.getJWTUserInfo()).thenReturn(Optional.of(userPrincipal()));
    }

    @After
    public void tearDown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void givenValidPostData_whenCreatingNewPost_thenPostIsSuccessfullyCreatedAndReturnsExpectedDetails() throws Exception {
        var postRequest = preparePostForRequest();
        Mockito.when(userRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(userEntityBasicInfo()));
        Mockito.when(categoryRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(makeCategoryForSaving("Category A")));
        Mockito.when(postRepository.save(Mockito.any(PostEntity.class)))
                .thenReturn(makePostForSaving());

        var expectedPostResponse = postMapper.toPostResponse(makePostForSaving());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .content(objectMapper.writeValueAsString(postRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedPostResponse.getId().toString()))
                .andExpect(jsonPath("$.title").value(expectedPostResponse.getTitle()));
    }

    @Test
    public void givenEmptyShortDescription_whenCreatingNewPost_thenReturnsBadRequestWithValidationErrorMessage() throws Exception {
        var postRequest = preparePostForRequest();
        postRequest.setShortDescription("");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .content(objectMapper.writeValueAsString(postRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // Expecting 400 Bad Request
                .andExpect(jsonPath("$.details.shortDescription").value("Short Description info cannot be blank"));

    }


}
