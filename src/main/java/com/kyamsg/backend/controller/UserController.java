package com.kyamsg.backend.controller;

import com.kyamsg.backend.dto.UserDtos.*;
import com.kyamsg.backend.security.CurrentUser;
import com.kyamsg.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(@CurrentUser UUID userId) {
        return userService.getProfile(userId);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@CurrentUser UUID userId, @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(userId, req);
    }
}
