package com.myblogbackend.blog.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ForgotPasswordRequest {
    @Email(message = "Email is required!")
    private String email;

    @NotBlank(message = "new password is required!")
    private String newPassword;
}
