package com.myblogbackend.blog.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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