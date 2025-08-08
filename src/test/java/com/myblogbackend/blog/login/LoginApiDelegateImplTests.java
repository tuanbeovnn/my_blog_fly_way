package com.myblogbackend.blog.login;

import static com.myblogbackend.blog.login.LoginTestApi.ONE_HOUR_IN_MILLIS;
import static com.myblogbackend.blog.login.LoginTestApi.createAuthenticationByLoginRequest;
import static com.myblogbackend.blog.login.LoginTestApi.jwtResponseForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.loginDataForRequesting;
import static com.myblogbackend.blog.login.LoginTestApi.mockJwtToken;
import static com.myblogbackend.blog.login.LoginTestApi.refreshTokenForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.IntegrationTestUtil;
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
    public void setUp() {
        SecurityContextHolder.clearContext();

        // Initialize test data using LoginTestApi helper methods
        loginDataRequest = loginDataForRequesting();
        userEntity = userEntityBasicInfo();
        // Set user as active since login method checks this
        userEntity.setActive(true);
        jwtToken = mockJwtToken();
        expirationDuration = ONE_HOUR_IN_MILLIS;
        refreshToken = refreshTokenForSaving();
        jwtResponse = jwtResponseForSaving(jwtToken, refreshToken.getToken(), expirationDuration);
    }

    @Test
    public void givenLoginData_whenSendData_thenReturnsJwtResponseCreated() throws Exception {
        // Mock user entity lookup
        Mockito.when(usersRepository.findByEmail(eq(loginDataRequest.getEmail()))).thenReturn(Optional.of(userEntity));

        // Create UsernamePasswordAuthenticationToken
        Authentication authentication = createAuthenticationByLoginRequest(loginDataRequest);

        // Mock authentication manager
        Mockito.when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);

        // Mock JWT token generation (requires UserEntity and deviceId)
        String deviceId = loginDataRequest.getDeviceInfo().getDeviceId();
        Mockito.when(jwtProvider.generateJwtToken(eq(userEntity), eq(deviceId))).thenReturn(jwtToken);

        // Create the refresh token
        Mockito.when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(refreshToken);

        // Test login api
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signin")
                .content(IntegrationTestUtil.asJsonString(loginDataRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(jwtToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken.getToken()));
    }
}