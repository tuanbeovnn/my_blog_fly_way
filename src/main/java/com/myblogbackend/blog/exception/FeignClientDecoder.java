package com.myblogbackend.blog.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FeignClientDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(FeignClientDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(final String feignClientClassName, final Response response) {
        String responseBody = extractResponseBody(response);

        log.error("FeignClientDecoder - Client: {}, Status: {}, Response: {}",
                feignClientClassName, response.status(), responseBody);

        if (response.status() >= HttpStatus.BAD_REQUEST.value()
                && response.status() < HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return new ExternalServiceException(
                    String.format("Error from %s - Status: %d, Message: %s",
                            feignClientClassName, response.status(), responseBody)
            );
        }

        return defaultErrorDecoder.decode(feignClientClassName, response);
    }

    private String extractResponseBody(final Response response) {
        if (response.body() == null) {
            return "No response body";
        }
        try {
            return new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read response body", e);
            return "Error reading response body";
        }
    }
}
