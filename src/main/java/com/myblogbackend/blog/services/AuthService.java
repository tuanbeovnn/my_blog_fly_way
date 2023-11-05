package com.myblogbackend.blog.services;

import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.JwtResponse;
import com.myblogbackend.blog.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    JwtResponse userLogin(LoginFormRequest loginFormRequest);

    UserResponse registerUser(SignUpFormRequest signUpRequest);

    JwtResponse refreshJwtToken(TokenRefreshRequest tokenRefreshRequest);
}
