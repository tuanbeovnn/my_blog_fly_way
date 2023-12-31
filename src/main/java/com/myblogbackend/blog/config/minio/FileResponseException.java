package com.myblogbackend.blog.config.minio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileResponseException extends RuntimeException {

    public FileResponseException(final String msg) {
        super(msg);
    }
}
