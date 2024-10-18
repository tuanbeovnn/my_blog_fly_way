package com.myblogbackend.blog.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserFirebaseDeviceResponse {
    @NotBlank
    private String deviceToken;
    @NotBlank
    private UUID userId;
}
