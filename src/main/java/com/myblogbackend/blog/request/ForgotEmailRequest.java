package com.myblogbackend.blog.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@Builder
public class ForgotEmailRequest {
    @Email(message = "Email is required!")
   private String email;
}
