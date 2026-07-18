package com.kyamsg.backend.controller;

import com.kyamsg.backend.dto.AuthDtos.*;
import com.kyamsg.backend.security.CurrentUser;
import com.kyamsg.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest req) {
        authService.sendOtp(req.phoneNumber());
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest req,
                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.verifyOtp(req, httpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req,
                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refresh(req, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@CurrentUser UUID userId) {
        authService.logoutAllDevices(userId);
        return ResponseEntity.noContent().build();
    }
}
