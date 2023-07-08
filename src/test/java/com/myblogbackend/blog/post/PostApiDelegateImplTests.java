package com.myblogbackend.blog.post;

import com.myblogbackend.blog.IntegrationTestUtil;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.security.UserPrincipal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

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
    @Test
    public void testCreatePost() throws Exception {
        // Mock the Authentication object
        Authentication authentication = Mockito.mock(Authentication.class);
        UserPrincipal userPrincipal = new UserPrincipal(UUID.randomUUID(), "testuser@example.com", "password");
        Mockito.when(authentication.getPrincipal()).thenReturn(userPrincipal);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);  // Set authentication as authenticated
        Mockito.when(authentication.getDetails()).thenReturn(userPrincipal);  // Set the userPrincipal as details

        // Set the mock Authentication object in the SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock the category repository
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity();
        category.setId(categoryId);
        Mockito.when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Mock the user repository
        UserEntity user = new UserEntity();
        user.setId(userPrincipal.getId());
        Mockito.when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(user));

        // Mock the post repository
        PostRequest postRequest = new PostRequest();
        postRequest.setCategoryId(categoryId);
        PostEntity createdPost = new PostEntity();
        createdPost.setId(UUID.randomUUID());
        Mockito.when(postRepository.save(Mockito.any(PostEntity.class))).thenReturn(createdPost);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/posts")
                        .header("Authorization", "Bearer .eyJzdWIiOiJmb29AZW1haWwuY29tIiwiZXhwIjoxNjM4ODU1MzA1LCJpYXQiOjE2Mzg4MTkzMDV9.")
                        .content(IntegrationTestUtil.asJsonString(postRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


    }


}
