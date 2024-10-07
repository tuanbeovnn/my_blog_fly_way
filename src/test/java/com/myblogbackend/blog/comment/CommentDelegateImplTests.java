package com.myblogbackend.blog.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.mapper.CommentMapper;
import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.repositories.CommentRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.comment.CommentTestApi.*;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static com.myblogbackend.blog.login.LoginTestApi.userPrincipal;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CommentDelegateImplTests {
    private static final String API_URL = "/api/v1/comments";
    private static final String API_URL_UPDATE = "/api/v1/comments/{id}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentRepository commentRepository;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void givenValidCommentData_whenCreatingNewComment_thenReturnComment() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito.mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo).thenReturn(Optional.of(userPrincipal()));
            when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(makePostForSaving("Title B", "Description B")));

            CommentEntity savedCommentEntity = makeCommentForSaving("this post is great");
            when(commentRepository.saveAndFlush(any(CommentEntity.class))).thenReturn(savedCommentEntity);

            var expectedCommentResponse = commentMapper.toCommentResponse(savedCommentEntity);

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                            .content(objectMapper.writeValueAsString(prepareCommentRequest()))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.details.id").value(expectedCommentResponse.getId().toString()))
                    .andExpect(jsonPath("$.details.content").value(expectedCommentResponse.getContent()));
        }
    }

    @Test
    public void givenEmtpyPostId_whenCreatingNewComment_thenReturnsBadRequestWithValidationErrorMessage() throws Exception {
        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito.mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo).thenReturn(Optional.of(userPrincipal()));
            when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
            when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(makePostForSaving("Title B", "Description B")));

            var commentRequest = prepareCommentRequest();
            commentRequest.setContent("");

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                            .content(objectMapper.writeValueAsString(commentRequest))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error[0].message").value("Content cannot be blank"));

        }
    }

        @Test
        public void givenInvalidPostId_whenCreatingNewComment_thenReturnsNotFound() throws Exception {
            try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito.mockStatic(JWTSecurityUtil.class)) {
                jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo).thenReturn(Optional.of(userPrincipal()));
                when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));

                // Simulate non-existing postId by returning an empty Optional
                when(postRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

                var commentRequest = prepareCommentRequest();

                mockMvc.perform(MockMvcRequestBuilders.post(API_URL)
                                .content(objectMapper.writeValueAsString(commentRequest))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.message").value("Could not find the Id"));
            }
        }

        @Test
        public void givenCommentUpdate_whenUpdatingComment_thenReturnUpdatedComment() throws Exception {
            try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito.mockStatic(JWTSecurityUtil.class)) {
                var  commentId = UUID.randomUUID();
                jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo).thenReturn(Optional.of(userPrincipal()));
                when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(userEntityBasicInfo()));
                when(postRepository.findById(any(UUID.class))).thenReturn(Optional.of(makePostForSaving("Title B", "Description B")));
                when(commentRepository.findById(commentId)).thenReturn(Optional.of(exsitingCommentEntity()));
                // create an update request object
                var updateComment = prepareCommentRequest();
                updateComment.setContent("Update comment");
                // mock object after saving
                var updateCommentAfterSave = makeCommentForSaving("Update comment");
                // save update object
                when(commentRepository.save(any(CommentEntity.class))).thenReturn(updateCommentAfterSave);
                // response object after saving
                var expectedCommentResponse = commentMapper.toCommentResponse(updateCommentAfterSave);

                mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/comments/{commentId}", commentId)
                                .content(objectMapper.writeValueAsString(updateComment))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.details.content").value(expectedCommentResponse.getContent()));
            }
    }
}

