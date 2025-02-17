package com.myblogbackend.blog.request;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgotEmailRequest {
    @Email(message = "Email is required!")
    private String email;
}
