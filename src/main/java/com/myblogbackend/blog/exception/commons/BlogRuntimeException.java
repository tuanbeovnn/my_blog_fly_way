package com.myblogbackend.blog.exception.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Setter
@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BlogRuntimeException extends RuntimeException {
    private String code;
    private String message;
    private HttpStatus status;
    public BlogRuntimeException(final CommonErrorCode code) {
        this.code = code.code();
        this.message = code.message();
        this.status = code.status();
    }

}