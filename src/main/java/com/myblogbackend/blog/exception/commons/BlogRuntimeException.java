package com.myblogbackend.blog.exception.commons;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Setter
@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BlogRuntimeException extends RuntimeException {
    private String message;
    private HttpStatus status;

    public BlogRuntimeException(final CommonErrorCode code, final Object... messageArgs) {
        if (messageArgs != null && messageArgs.length > 0) {
            String additionalMessage = messageArgs[0] != null ? messageArgs[0].toString() : "";
            this.message = code.message() + " " + additionalMessage;
        } else {
            this.message = code.message();
        }
        this.status = code.status();
    }

}