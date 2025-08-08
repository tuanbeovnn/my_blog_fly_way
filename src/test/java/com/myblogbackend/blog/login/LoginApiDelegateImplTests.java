package com.myblogbackend.blog.login;

import static com.myblogbackend.blog.login.LoginTestApi.ONE_HOUR_IN_MILLIS;
import static com.myblogbackend.blog.login.LoginTestApi.createAuthenticationByLoginRequest;
import static com.myblogbackend.blog.login.LoginTestApi.googleOAuthRequestForTesting;
import static com.myblogbackend.blog.login.LoginTestApi.googleUserEntityForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.jwtResponseForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.loginDataForRequesting;
import static com.myblogbackend.blog.login.LoginTestApi.mockExchangeTokenResponse;
import static com.myblogbackend.blog.login.LoginTestApi.mockGoogleUserResponse;
import static com.myblogbackend.blog.login.LoginTestApi.mockJwtToken;
import static com.myblogbackend.blog.login.LoginTestApi.refreshTokenForSaving;
import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.myblogbackend.blog.enums.RoleName;
import com.myblogbackend.blog.models.RoleEntity;
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
import com.myblogbackend.blog.feign.OutboundIdentityClient;
import com.myblogbackend.blog.feign.OutboundUserClient;
import com.myblogbackend.blog.models.RefreshTokenEntity;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.repositories.RefreshTokenRepository;
import com.myblogbackend.blog.repositories.RoleRepository;
import com.myblogbackend.blog.repositories.UserDeviceRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.LoginFormOutboundRequest;
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
    @MockitoBean
    private OutboundIdentityClient outboundIdentityClient;
    @MockitoBean
    private OutboundUserClient outboundUserClient;
    @MockitoBean
    private RoleRepository roleRepository;
    @MockitoBean
    private UserDeviceRepository userDeviceRepository;
    private LoginFormRequest loginDataRequest;
    private LoginFormOutboundRequest googleOAuthRequest;
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
        googleOAuthRequest = googleOAuthRequestForTesting();
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

    @Test
    public void givenGoogleOAuthData_whenAuthenticating_thenReturnsJwtResponseCreated() throws Exception {
        // Create a new Google user for OAuth test
        var googleUser = googleUserEntityForSaving();
        var mockExchangeResponse = mockExchangeTokenResponse();
        var mockUserResponse = mockGoogleUserResponse();

        // Mock OAuth token exchange
        Mockito.when(outboundIdentityClient.exchangeToken(any())).thenReturn(mockExchangeResponse);

        // Mock Google user info retrieval
        Mockito.when(outboundUserClient.getUserInfo(eq("json"), eq(mockExchangeResponse.getAccessToken())))
                .thenReturn(mockUserResponse);

        // Mock user repository to return empty (new user scenario)
        Mockito.when(usersRepository.findByEmail(eq(mockUserResponse.getEmail()))).thenReturn(Optional.empty());

        // Mock role repository for createNewUser method
        var userRole = new RoleEntity();
        userRole.setName(RoleName.ROLE_USER);
        Mockito.when(roleRepository.findByName(eq(RoleName.ROLE_USER)))
                .thenReturn(Optional.of(userRole));

        // Mock user save for new user creation
        Mockito.when(usersRepository.save(any(UserEntity.class))).thenReturn(googleUser);

        // Mock UserDeviceRepository for createRefreshToken method
        Mockito.when(userDeviceRepository.findByUserId(any())).thenReturn(Optional.empty());

        // Mock JWT token generation for Google user
        String deviceId = googleOAuthRequest.getDeviceInfo().getDeviceId();
        Mockito.when(jwtProvider.generateJwtToken(any(UserEntity.class), eq(deviceId))).thenReturn(jwtToken);

        // Mock refresh token creation
        Mockito.when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenReturn(refreshToken);

        // Test Google OAuth authentication
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/identity/outbound/authentication")
                .content(IntegrationTestUtil.asJsonString(googleOAuthRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(jwtToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken.getToken()));
    }
}