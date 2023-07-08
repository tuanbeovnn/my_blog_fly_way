package com.myblogbackend.blog.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.IntegrationTestUtil;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.security.JwtProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.ResponseBodyMatcher.responseBody;
import static com.myblogbackend.blog.login.LoginTestApi.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginApiDelegateImplTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UsersRepository usersRepository;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    private LoginFormRequest loginDataRequest;
    private String jwtToken;
    private long expirationDuration;
    private RefreshTokenEntity refreshToken;
    private UserEntity userEntity;
    private JwtResponse jwtResponse;

    @BeforeEach
    public void setupContext() {
        SecurityContextHolder.clearContext();
    }

    @Before
    public void setup() {
        UUID userId = UUID.randomUUID();
        loginDataRequest = loginDataForRequesting();
        jwtToken = mockJwtToken();
        String password = "123456";
        expirationDuration = 3600000L;
        userEntity = userEntityForSaving(userId, passwordEncoder.encode(password));
        refreshToken = createRefreshTokenEntity(loginDataRequest, userEntity);
        jwtResponse = jwtResponseForSaving(jwtToken, refreshToken.getToken(), expirationDuration);
    }

    @Test
    public void givenLoginData_whenSendData_thenReturnsJwtResponseCreated() throws Exception {
        //mock user login data
        //mock user entity
        Mockito.when(usersRepository.findByEmail(loginDataRequest.getEmail())).thenReturn(Optional.of(userEntity));
        //create UsernamePasswordAuthenticationToken
        Authentication authentication = createAuthenticationByLoginRequest(loginDataRequest);
        //mock authentication manager
        Mockito.when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        //set the authentication to SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //create the Jwt token from JwtProvider
        Mockito.when(jwtProvider.generateJwtToken(authentication)).thenReturn(jwtToken);
        //create the refresh token
        Mockito.when(refreshTokenRepository.save(Mockito.any())).thenReturn(refreshToken);
        //create expiration from jwt provider
        Mockito.when(jwtProvider.getExpiryDuration()).thenReturn(expirationDuration);
        //create jwt response after login successfully
        //test login api
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                        .content(IntegrationTestUtil.asJsonString(loginDataRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.accessToken", is(jwtToken)))
                .andExpect(jsonPath("$.refreshToken", is(refreshToken.getToken())))
                .andExpect(jsonPath("$.expiryDuration").value((int) expirationDuration))
                .andExpect(responseBody().containsObjectBody(jwtResponse, JwtResponse.class, objectMapper));
    }
}