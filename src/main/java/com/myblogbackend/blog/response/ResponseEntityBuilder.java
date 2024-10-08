package com.myblogbackend.blog.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ResponseEntityBuilder {
    private final Map<String, Object> map = new HashMap<>();
    private final List<Map<String, String>> errors = new ArrayList<>();

    private ResponseEntityBuilder() {
        map.put("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());
        this.setCode(200); // Default code
    }

    public static ResponseEntityBuilder getBuilder() {
        return new ResponseEntityBuilder();
    }

    public ResponseEntityBuilder setCode(final int code) {
        map.put("code", code);
        setSuccess(code < 400); // Success if the status code is less than 400
        return this;
    }

    public ResponseEntityBuilder setCode(final HttpStatus code) {
        return this.setCode(code.value());
    }

    public ResponseEntityBuilder setMessage(final String message) {
        map.put("message", message); // Set the message field

        // Only add an error message if the code indicates failure
        if (map.get("code") != null && (Integer) map.get("code") >= 400) {
            Map<String, String> errorMessage = new HashMap<>();
            errorMessage.put("message", message);
            errors.clear();  // Clear previous errors
            errors.add(errorMessage);
            map.put("error", errors); // Set the error list
        } else {
            // If success, ensure the error field is not included
            map.remove("error"); // Remove the error field if it's a success response
        }

        return this;
    }

    public ResponseEntityBuilder set(final String key, final Object value) {
        map.put(key, value);
        return this;
    }

    public void setSuccess(final boolean isSuccess) {
        map.put("success", isSuccess);
    }

    public ResponseEntityBuilder setDetails(final Object details) {
        map.put("details", details);
        return this;
    }

    public ResponseEntity<?> build() {
        if (map.get("message") == null && map.get("code") != null && (Integer) map.get("code") >= 400) {
            // Set a default error message if not provided
            this.setMessage(HttpStatus.valueOf((Integer) map.get("code")).getReasonPhrase());
        }

        int code = (Integer) map.get("code");
        return ResponseEntity.status(HttpStatus.valueOf(code))
                .body(map); // Return the final map as the response body
    }
}