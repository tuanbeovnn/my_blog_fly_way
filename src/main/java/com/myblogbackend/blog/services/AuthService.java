package com.myblogbackend.blog.services;

import com.myblogbackend.blog.enums.NotificationType;
import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.request.LoginFormOutboundRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.response.UserResponse;
import freemarker.template.TemplateException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthService {
    JwtResponse userLogin(LoginFormRequest loginFormRequest);

    UserResponse registerUser(SignUpFormRequest signUpRequest) throws TemplateException, IOException;

    JwtResponse refreshJwtToken(TokenRefreshRequest tokenRefreshRequest);

    ResponseEntity<?> confirmationEmail(String token) throws IOException;

    void createVerificationToken(UserEntity userEntity, String token, NotificationType notificationType);

    JwtResponse outboundAuthentication(final LoginFormOutboundRequest loginFormOutboundRequest);


}
