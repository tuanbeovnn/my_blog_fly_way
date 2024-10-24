package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.FcmTokenRoutes;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.impl.FcmTokenServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + FcmTokenRoutes.BASE_URL)
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenServiceImpl fcmTokenService;

    @PutMapping("/token")
    public ResponseEntity<?> saveFcmUserTokenDevice(@RequestBody @Valid final UserFirebaseDeviceRequest userFirebaseDeviceRequest) {
        var userFirebaseDeviceResponse = fcmTokenService.saveFcmUserTokenDevice(userFirebaseDeviceRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(userFirebaseDeviceResponse)
                .build();
    }
}
