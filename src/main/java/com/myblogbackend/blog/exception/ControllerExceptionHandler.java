package com.myblogbackend.blog.exception;

import com.myblogbackend.blog.response.ResponseEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
public class ControllerExceptionHandler {
    private final static Logger LOGGER = LogManager.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<?> handleException(final MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        for (FieldError fieldError : errors) {
            details.putIfAbsent(fieldError.getField(), "");
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
            LOGGER.warn("Validation error for field '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntityBuilder.getBuilder()
                .setCode(HttpStatus.BAD_REQUEST)
                .setMessage("Validation errors")
                .setDetails(details)
                .build();
    }

}