package com.myblogbackend.blog.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JwtResponse {
    private final String accessToken;
    private final String refreshToken;
    private static final String tokenType = "Bearer ";

    public JwtResponse(final String accessToken, final String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}