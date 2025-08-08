package com.myblogbackend.blog.request;

import com.myblogbackend.blog.validation.PasswordStrength;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotEmpty
    private String oldPassword;

    @NotEmpty
    @PasswordStrength
    private String newPassword;

    @NotEmpty
    private String confirmPassword;
}
