package com.myblogbackend.blog.exception.commons;

import org.springframework.http.HttpStatus;

public interface CommonErrorCode {
    HttpStatus status();

    String message();
}