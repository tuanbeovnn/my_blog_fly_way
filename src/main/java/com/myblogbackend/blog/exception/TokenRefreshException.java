package com.myblogbackend.blog.exception;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class TokenRefreshException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;
	private final String token;
    private final String message;

    public TokenRefreshException(final String token, final String message) {
        super(String.format("Couldn't refresh token for [%s]: [%s])", token, message));
        this.token = token;
        this.message = message;
    }
}