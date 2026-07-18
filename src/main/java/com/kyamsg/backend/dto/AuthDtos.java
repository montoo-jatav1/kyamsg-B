package com.kyamsg.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthDtos {

    public record SendOtpRequest(
            @NotBlank
            @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$", message = "must be in E.164 format, e.g. +919876543210")
            String phoneNumber
    ) {}

    public record VerifyOtpRequest(
            @NotBlank String phoneNumber,
            @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "must be a 6-digit code") String otp,
            String deviceId,
            String deviceName
    ) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record LogoutRequest(@NotBlank String refreshToken) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            boolean isNewUser,
            UserDto user
    ) {}

    public record UserDto(
            String id,
            String phoneNumber,
            String name,
            String username,
            String about,
            String profilePhotoUrl,
            String language
    ) {}
}
