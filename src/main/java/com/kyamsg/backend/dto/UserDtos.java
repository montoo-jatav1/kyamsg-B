package com.kyamsg.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserDtos {

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 80) String name,
            @Pattern(regexp = "^[a-z0-9_.]{3,40}$", message = "3-40 chars: lowercase letters, numbers, _ or .") String username,
            @Email String recoveryEmail,
            String language
    ) {}

    public record UserResponse(
            String id,
            String phoneNumber,
            String name,
            String username,
            String about,
            String profilePhotoUrl,
            String recoveryEmail,
            String language
    ) {}
}
