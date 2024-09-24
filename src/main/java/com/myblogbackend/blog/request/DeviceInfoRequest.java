package com.myblogbackend.blog.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeviceInfoRequest {

    @NotBlank(message = "Device id cannot be blank")
    private String deviceId;

    @NotNull(message = "Device type cannot be null")
    private String deviceType;
}