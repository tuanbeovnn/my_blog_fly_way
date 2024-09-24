package com.myblogbackend.blog.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginFormOutboundRequest {
    @NotBlank
    private String code;

    @Valid
    @NotNull(message = "Device info cannot be null")
    private DeviceInfoRequest deviceInfo;
}