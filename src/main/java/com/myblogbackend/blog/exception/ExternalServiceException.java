package com.myblogbackend.blog.exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(final String message) {
        super(message);
    }
}
