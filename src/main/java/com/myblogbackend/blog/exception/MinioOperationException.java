package com.myblogbackend.blog.exception;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(final String message) {
        super(message);
    }

    public MinioOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
