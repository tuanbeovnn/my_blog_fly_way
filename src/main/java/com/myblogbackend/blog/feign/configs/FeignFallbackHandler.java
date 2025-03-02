package com.myblogbackend.blog.feign.configs;

import com.myblogbackend.blog.exception.ExternalServiceException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class FeignFallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(FeignFallbackHandler.class);

    public static <T> T handleFallback(final Throwable exception) {
        logger.error("Fallback triggered. Exception: ", exception);

        Throwable rootCause = exception.getCause();

        if (rootCause instanceof ConnectException ||
                rootCause instanceof SocketTimeoutException ||
                rootCause instanceof UnknownHostException) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "External Service Unavailable: " + rootCause.getMessage());
        }

        if (exception instanceof FeignException feignException) {
            if (feignException.status() == -1) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "External Service Unavailable: " + feignException.getMessage());
            }

            HttpStatus httpStatus = HttpStatus.resolve(feignException.status());
            if (httpStatus == null) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            throw new ResponseStatusException(httpStatus,
                    "External Service Failure: " + feignException.getMessage());
        }

        if (exception instanceof ExternalServiceException externalServiceException) {
            String message = externalServiceException.getMessage();
            if (message.contains("Status: 400")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            } else if (message.contains("Status: 401")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
            } else if (message.contains("Status: 500")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error in fallback");
    }
}
