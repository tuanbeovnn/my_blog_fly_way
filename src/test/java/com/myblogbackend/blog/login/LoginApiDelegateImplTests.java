package com.myblogbackend.blog.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.config.security.JwtProvider;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.response.JwtResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginApiDelegateImplTests {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtProvider jwtProvider;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UsersRepository usersRepository;
    @MockitoBean
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

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        jwtResponse = JwtResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .build();
    }

    @Test
    public void givenLoginData_whenSendData_thenReturnsJwtResponseCreated() throws Exception {
        // //mock user login data
        // //mock user entity
        // Mockito.when(usersRepository.findByEmail(loginDataRequest.getEmail())).thenReturn(Optional.of(userEntity));
        // //create UsernamePasswordAuthenticationToken
        // Authentication authentication =
        // createAuthenticationByLoginRequest(loginDataRequest);
        // //mock authentication manager
        // Mockito.when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        // //set the authentication to SecurityContextHolder
        // SecurityContextHolder.getContext().setAuthentication(authentication);
        // //create the Jwt token from JwtProvider
        // Mockito.when(jwtProvider.generateJwtToken(authentication)).thenReturn(jwtToken);
        // //create the refresh token
        // Mockito.when(refreshTokenRepository.save(Mockito.any())).thenReturn(refreshToken);
        // //create expiration from jwt provider
        // Mockito.when(jwtProvider.getExpiryDuration()).thenReturn(expirationDuration);
        // //create jwt response after login successfully
        // //test login api
        // mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
        // .content(IntegrationTestUtil.asJsonString(loginDataRequest))
        // .contentType(MediaType.APPLICATION_JSON))
        // .andExpect(MockMvcResultMatchers.status().isOk())
        // .andExpect(jsonPath("$.accessToken", is(jwtToken)))
        // .andExpect(jsonPath("$.refreshToken", is(refreshToken.getToken())))
        // .andExpect(jsonPath("$.expiryDuration").value((int) expirationDuration))
        // .andExpect(responseBody().containsObjectBody(jwtResponse, JwtResponse.class,
        // objectMapper));
    }
}