package com.myblogbackend.blog.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = false)
public class ForgotPasswordRequest {
    @Email(message = "Email is required!")
    private String email;
}
