package com.myblogbackend.blog.exception;

import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import feign.RetryableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestControllerAdvice
public class ControllerExceptionHandler {
    private final static Logger logger = LogManager.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(JwtTokenExpiredException.class)
    @ResponseBody
    public ResponseEntity<?> handleJwtTokenExpiredException(final JwtTokenExpiredException ex) {
        logger.warn("JWT token expired: {}", ex.getMessage());
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.UNAUTHORIZED)
                .setMessage("Expired JWT token")
                .set("error", List.of(Map.of("message", "Expired JWT token")))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleException(final MethodArgumentNotValidException ex) {
        List<Map<String, String>> errorDetails = new ArrayList<>();
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : errors) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            errorDetails.add(error);
            logger.warn("Validation error for field '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.BAD_REQUEST)
                .setMessage("Validation errors")
                .set("error", errorDetails)
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<?> processMethodNotSupportedException(final HttpRequestMethodNotSupportedException exception) {
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.METHOD_NOT_ALLOWED)
                .setMessage(exception.getMessage())
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<?> processAccessDeniedException(final AccessDeniedException e) {
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.FORBIDDEN)
                .setMessage(e.getMessage())
                .build();
    }

    @ExceptionHandler(value = BlogRuntimeException.class)
    @ResponseBody
    public ResponseEntity<?> handler(final BlogRuntimeException e, final HttpServletRequest request) {
        return ResponseEntityBuilder
                .getBuilder()
                .setCode(Integer.parseInt(String.valueOf(e.getStatus().value())))
                .setMessage(e.getMessage())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<?> handleConstraintViolationException(final ConstraintViolationException e) {
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.BAD_REQUEST)
                .setMessage(e.getMessage())
                .build();
    }

    @ExceptionHandler(RetryableException.class)
    @ResponseBody
    public ResponseEntity<?> handleRetryableException(final RetryableException e) {
        logger.error("Feign client encountered a RetryableException: {}", e.getMessage());
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.SERVICE_UNAVAILABLE)
                .setMessage("Service temporarily unavailable" + e.getMessage())
                .build();
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<?> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request body: " + ex.getLocalizedMessage();
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.BAD_REQUEST)
                .setMessage(errorMessage)
                .build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(final ResponseStatusException ex) {
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }
}