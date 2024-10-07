package com.myblogbackend.blog.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@Setter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtTokenExpiredException extends RuntimeException {
    public JwtTokenExpiredException(final String message) {
        super(message);
    }
}
