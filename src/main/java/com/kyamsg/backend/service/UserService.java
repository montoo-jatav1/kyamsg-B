package com.kyamsg.backend.service;

import com.kyamsg.backend.dto.UserDtos.*;
import com.kyamsg.backend.entity.User;
import com.kyamsg.backend.exception.ApiException;
import com.kyamsg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = findUser(userId);

        if (req.username() != null && !req.username().equals(user.getUsername())
                && userRepository.existsByUsername(req.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }

        user.setName(req.name());
        if (req.username() != null) user.setUsername(req.username());
        if (req.recoveryEmail() != null) user.setRecoveryEmail(req.recoveryEmail());
        if (req.language() != null) user.setLanguage(req.language());

        userRepository.save(user);
        return toResponse(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId().toString(), u.getPhoneNumber(), u.getName(), u.getUsername(),
                u.getAbout(), u.getProfilePhotoUrl(), u.getRecoveryEmail(), u.getLanguage()
        );
    }
}
