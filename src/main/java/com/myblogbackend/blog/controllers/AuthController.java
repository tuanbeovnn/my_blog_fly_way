package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.AuthRoutes;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.ForgotPasswordRequest;
import com.myblogbackend.blog.request.LoginFormOutboundRequest;
import com.myblogbackend.blog.request.LoginFormRequest;
import com.myblogbackend.blog.request.SignUpFormRequest;
import com.myblogbackend.blog.request.TokenRefreshRequest;
import com.myblogbackend.blog.response.ApiResponse;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + AuthRoutes.BASE_URL)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/identity/outbound/authentication")
    public ResponseEntity<?> login(@RequestBody final LoginFormOutboundRequest loginFormOutboundRequest) {
        var jwtResponse = authService.outboundAuthentication(loginFormOutboundRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(final @Valid @RequestBody LoginFormRequest loginRequest) {
        var jwtResponse = authService.userLogin(loginRequest);
        if (Objects.isNull(jwtResponse)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "User has been deactivated/locked !!"));
        }
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(final @Valid @RequestBody SignUpFormRequest signUpRequest) {
        authService.registerUserV2(signUpRequest);

        var responseBuilder = ResponseEntityBuilder.getBuilder()
                .setCode(200)
                .setMessage("Registration successful. Please check your email to verify your account.")
                .set("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());

        return ResponseEntity.ok().body(responseBuilder.build());
    }

    @GetMapping("/registrationConfirm")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") final String token) {
        try {
            return authService.confirmationEmail(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshJwtToken(final @Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        var jwtResponse = authService.refreshJwtToken(tokenRefreshRequest);
        return ResponseEntity.ok().body(jwtResponse);
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody final ForgotPasswordRequest forgotPasswordDto) {
        var responseBuilder = ResponseEntityBuilder.getBuilder()
                .setCode(200)
                .setMessage("Sent email successfully!")
                .set("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());

        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping("/send-email-forgot-password")
    public ResponseEntity<?> sendEmailForgotPassword(@RequestParam("email") final String email) {
        authService.sendEmailForgotPassword(email);
        var responseBuilder = ResponseEntityBuilder.getBuilder()
                .setCode(200)
                .setMessage("Sent email successfully!")
                .set("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());

        return ResponseEntity.ok(responseBuilder.build());
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam final String email, @RequestParam final String token) {
        authService.resetPassword(email, token);

        return ResponseEntity.ok(Map.of(
                "message", "A new password has been sent to your email.",
                "timestamp", Instant.now().toString()
        ));
    }
}