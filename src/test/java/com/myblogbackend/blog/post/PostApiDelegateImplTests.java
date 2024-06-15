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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
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
import static com.myblogbackend.blog.post.PostTestApi.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    public void givenValidPostData_whenCreatingNewPost_thenPostIsSuccessfullyCreatedAndReturnsExpectedDetails() throws Exception {

        var postRequest = preparePostForRequest();
        Mockito.mockStatic(JWTSecurityUtil.class);
        Mockito.when(JWTSecurityUtil.getJWTUserInfo()).thenReturn(Optional.of(userPrincipal()));
        Mockito.when(userRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(userEntityBasicInfo()));
        Mockito.when(categoryRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(makeCategoryForSaving("Category A")));
        Mockito.when(postRepository.save(any(PostEntity.class)))
                .thenReturn(makePostForSaving("Title A", "Description A"));

        var expectedPostResponse = postMapper.toPostResponse(makePostForSaving("Title A", "Description A"));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.shortDescription").value("Short Description info cannot be blank"));
    }

    @Test
    public void givenUserRequestForListPost_whenRequestListPost_thenReturnsListPost() throws Exception {
        var postEntityList = preparePostsEntitySaving();

        Mockito.when(postRepository.findAll(Mockito.<Specification<PostEntity>>any(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(postEntityList));

        var expectedPostList = postMapper.toListPostResponse(postEntityList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/public/posts/feed")
                        .param("page", "0")
                        .param("size", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(postEntityList.size())))
                .andExpect(jsonPath("$.details.records[0].title", is(expectedPostList.get(0).getTitle())))
                .andExpect(jsonPath("$.details.records[1].title", is(expectedPostList.get(1).getTitle())));
    }
}