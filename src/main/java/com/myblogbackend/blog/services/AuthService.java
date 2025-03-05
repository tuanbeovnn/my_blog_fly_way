package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.LoginFormOutboundRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthService {
    JwtResponse userLogin(LoginFormRequest loginFormRequest);

    void registerUserV2(SignUpFormRequest signUpRequest) throws IOException;

    JwtResponse refreshJwtToken(TokenRefreshRequest tokenRefreshRequest);

    ResponseEntity<?> confirmationEmail(String token) throws IOException;

    JwtResponse outboundAuthentication(final LoginFormOutboundRequest loginFormOutboundRequest);

    ResponseEntity<?>  resetPassword(final String email, final String token) throws IOException;

    ResponseEntity<?> sendEmailForgotPassword(String email);

}
