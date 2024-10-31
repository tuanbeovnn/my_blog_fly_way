package com.myblogbackend.blog.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserFirebaseDeviceRequest {
    @NotBlank
    private String deviceToken;
    private UUID userId;
}
