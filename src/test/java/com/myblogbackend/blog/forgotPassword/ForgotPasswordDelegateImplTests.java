package com.myblogbackend.blog.forgotPassword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.models.UserVerificationTokenEntity;
import com.myblogbackend.blog.repositories.UserTokenRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.request.ForgotPasswordRequest;
import com.myblogbackend.blog.request.MailRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.UUID;

import static com.myblogbackend.blog.forgotPassword.ForgotPasswordTestApi.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ForgotPasswordDelegateImplTests {
    private static final String API_URL_FORGOT_PASSWORD = "/api/v1/auth/forgot-password";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersRepository usersRepository;

    @MockBean
    private UserTokenRepository userTokenRepository;

    @MockBean
    private KafkaTemplate<String, MailRequest> kafkaTemplate;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidRequest_whenForgotPassword_thenReturnSuccess() throws Exception {
        UserEntity user = createActiveUser();
        UserVerificationTokenEntity userVerificationTokenEntity =  createValidToken(user);
        var newPassword = "newpassword";
        var hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);


        when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userTokenRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(userVerificationTokenEntity));
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(usersRepository.save(any(UserEntity.class))).thenReturn(user);

        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail(user.getEmail());
        forgotPasswordRequest.setNewPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FORGOT_PASSWORD)
                .param("token", userVerificationTokenEntity.getVerificationToken())
                .content(objectMapper.writeValueAsString(forgotPasswordRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(contains("Your currently password")));
    }

    @Test
    public void givenInValidToken_whenForgotPassword_thenReturnInvalidTemplates() throws Exception {
        UserEntity user = createActiveUser();
        var newPassword = "newpassword";
        var hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);


        when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(userTokenRepository.findByVerificationToken(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(usersRepository.save(any(UserEntity.class))).thenReturn(user);

        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail(user.getEmail());
        forgotPasswordRequest.setNewPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FORGOT_PASSWORD)
                        .param("token", "invalid-token")
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(contains("The link is invalid or expired.")));
    }

    @Test
    public void givenInActiveUser_whenForgotPassword_thenReturnBadRequest() throws Exception {
        UserEntity inActiveUser = createInActiveUser();
        UserVerificationTokenEntity userVerificationTokenEntity =  createValidToken(inActiveUser);
        var newPassword = "newpassword";
        var hashedNewPassword = passwordEncoder.encode(newPassword);
        inActiveUser.setPassword(hashedNewPassword);

        when(usersRepository.findById(any(UUID.class))).thenReturn(Optional.of(inActiveUser));
        when(userTokenRepository.findByVerificationToken(anyString())).thenReturn(Optional.of(userVerificationTokenEntity));
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(usersRepository.save(any(UserEntity.class))).thenReturn(inActiveUser);

        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        forgotPasswordRequest.setEmail(inActiveUser.getEmail());
        forgotPasswordRequest.setNewPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(API_URL_FORGOT_PASSWORD)
                        .param("token", userVerificationTokenEntity.getVerificationToken())
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[0].message").value("Account has not active yet"));
    }
}
