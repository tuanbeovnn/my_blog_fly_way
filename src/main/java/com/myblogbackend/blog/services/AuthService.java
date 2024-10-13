package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.*;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.response.UserResponse;
import freemarker.template.TemplateException;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface AuthService {
    JwtResponse userLogin(LoginFormRequest loginFormRequest);

    UserResponse registerUserV2(SignUpFormRequest signUpRequest) throws TemplateException, IOException;


    JwtResponse refreshJwtToken(TokenRefreshRequest tokenRefreshRequest);

    ResponseEntity<?> confirmationEmail(String token) throws IOException;

    JwtResponse outboundAuthentication(final LoginFormOutboundRequest loginFormOutboundRequest);

    void forgotPassword(ForgotPasswordRequest forgotPasswordDto);


}
