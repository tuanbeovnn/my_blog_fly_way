package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.ForgotPasswordRequest;
import com.myblogbackend.blog.request.LoginFormOutboundRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthService {
    JwtResponse userLogin(LoginFormRequest loginFormRequest);

    void registerUserV2(SignUpFormRequest signUpRequest);

    JwtResponse refreshJwtToken(TokenRefreshRequest tokenRefreshRequest);

    ResponseEntity<?> confirmationEmail(String token) throws IOException;

    JwtResponse outboundAuthentication(final LoginFormOutboundRequest loginFormOutboundRequest);

    void forgotPassword(ForgotPasswordRequest forgotPasswordDto);

    void handleForgotPassword(ForgotPasswordRequest forgotPasswordRequest, String token) throws IOException;

    void sendEmailForgotPassword(String email);

}
