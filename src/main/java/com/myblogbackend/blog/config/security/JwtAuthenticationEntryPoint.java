package com.myblogbackend.blog.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(final HttpServletRequest request,
                         final HttpServletResponse response,
                         final AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message;
        Exception exception = (Exception) request.getAttribute("exception");

        if (exception != null) {
            message = exception.getMessage();
        } else {
            if (authException.getCause() != null) {
                message = authException.getCause().toString() + ": " + authException.getMessage();
            } else {
                message = authException.getMessage();
            }
        }

        // Construct the response body in the desired format
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", HttpServletResponse.SC_FORBIDDEN);
        responseBody.put("success", false);
        responseBody.put("message", message);
        responseBody.put("error", List.of(Map.of("message", message)));
        responseBody.put("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());

        // Write the response
        ObjectMapper mapper = new ObjectMapper();
        response.getOutputStream().write(mapper.writeValueAsBytes(responseBody));
    }
}