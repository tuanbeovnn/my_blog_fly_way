package com.myblogbackend.blog.favorite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.models.FavoriteEntity;

import com.myblogbackend.blog.repositories.CommentRepository;
import com.myblogbackend.blog.repositories.FavoriteRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.comment.CommentTestApi.makeCommentForSaving;

import static com.myblogbackend.blog.favorite.FavoriteTestApi.makeFavoriteCommentForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userPrincipal;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class FavoriteDelegateImplTests {
    private static final String API_URL_FAVORITE_COMMENT = "/api/v1/favorites/comment";
    private static final String API_URL_FAVORITE_POST = "/api/v1/favorites/post";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private FavoriteRepository favoriteRepository;

    @MockitoBean
    private UsersRepository usersRepository;

    @MockitoBean
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID targetId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    public void givenNonExistingCommentFavorite_whenCreatingNewFavorite_thenIncrementLike() throws Exception {
        var commentEntity = makeCommentForSaving("this post is great");
        var favoriteEntity = makeFavoriteCommentForSaving();

        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(commentRepository.findById(targetId)).thenReturn(Optional.of(commentEntity));
            when(favoriteRepository.findByUserIdAndCommentId(userId, targetId))
                    .thenReturn(Optional.empty());
            when(favoriteRepository.save(any(FavoriteEntity.class))).thenReturn(favoriteEntity);

            commentEntity.setLikes(commentEntity.getLikes() + 1);
            mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FAVORITE_COMMENT + "/{targetId}", targetId)
                            .param("type", "LIKE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void givenExistedCommentFavorite_whenCreatingNewFavorite_thenDecrementLike() throws Exception {
        var commentEntity = makeCommentForSaving("this post is great");
        var favoriteEntity = makeFavoriteCommentForSaving();

        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(commentRepository.findById(targetId)).thenReturn(Optional.of(commentEntity));
            when(favoriteRepository.findByUserIdAndCommentId(userId, targetId))
                    .thenReturn(Optional.ofNullable(favoriteEntity));
            when(favoriteRepository.deleteByUserIdAndCommentId(userId,  targetId )).thenReturn(1);

            commentEntity.setLikes(Math.max(commentEntity.getLikes() - 1,  0 ));

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FAVORITE_COMMENT + "/{targetId}", targetId)
                            .param("type", "LIKE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
    @Test
    public void givenNonExistingPostFavorite_whenCreatingNewFavorite_thenIncrementLike() throws Exception {
        var postEntity = makePostForSaving("Title A", "Description A");
        postEntity.setFavourite(0L);
        var favoriteEntity = makeFavoriteCommentForSaving();

        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(postRepository.findById(targetId)).thenReturn(Optional.of(postEntity));
            when(favoriteRepository.findByUserIdAndCommentId(userId, targetId))
                    .thenReturn(Optional.empty());
            when(favoriteRepository.save(any(FavoriteEntity.class))).thenReturn(favoriteEntity);

            postEntity.setFavourite(postEntity.getFavourite() + 1);

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FAVORITE_POST + "/{targetId}", targetId)
                            .param("type", "LIKE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Test
    public void givenExistedPostFavorite_whenCreatingNewFavorite_thenDecrementLike() throws Exception {
        var postEntity = makePostForSaving("Title A", "Description A");
        postEntity.setFavourite(0L);

        var favoriteEntity = makeFavoriteCommentForSaving();

        try (MockedStatic<JWTSecurityUtil> jwtSecurityUtilMockedStatic = Mockito
                .mockStatic(JWTSecurityUtil.class)) {

            jwtSecurityUtilMockedStatic.when(JWTSecurityUtil::getJWTUserInfo)
                    .thenReturn(Optional.of(userPrincipal()));

            when(postRepository.findById(targetId)).thenReturn(Optional.of(postEntity));
            when(favoriteRepository.findByUserIdAndCommentId(userId, targetId))
                    .thenReturn(Optional.ofNullable(favoriteEntity));
            when(favoriteRepository.deleteByUserIdAndPostId(userId,  targetId )).thenReturn(1);

            postEntity.setFavourite(Math.max(postEntity.getFavourite() - 1,  0 ));

            mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FAVORITE_POST + "/{targetId}", targetId)
                            .param("type", "LIKE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
