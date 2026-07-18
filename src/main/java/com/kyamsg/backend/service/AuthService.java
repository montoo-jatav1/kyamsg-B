package com.kyamsg.backend.service;

import com.kyamsg.backend.dto.AuthDtos.*;
import com.kyamsg.backend.entity.User;
import com.kyamsg.backend.entity.UserSession;
import com.kyamsg.backend.exception.ApiException;
import com.kyamsg.backend.repository.UserRepository;
import com.kyamsg.backend.repository.UserSessionRepository;
import com.kyamsg.backend.security.JwtService;
import com.kyamsg.backend.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long REFRESH_TOKEN_TTL_DAYS = 30;

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void sendOtp(String phoneNumber) {
        otpService.sendOtp(phoneNumber);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest req, HttpServletRequest httpRequest) {
        otpService.verifyOtp(req.phoneNumber(), req.otp());

        boolean isNewUser = !userRepository.existsByPhoneNumber(req.phoneNumber());
        User user = userRepository.findByPhoneNumber(req.phoneNumber())
                .orElseGet(() -> userRepository.save(
                        User.builder().phoneNumber(req.phoneNumber()).build()));

        return issueSession(user, isNewUser, req.deviceId(), req.deviceName(), httpRequest);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req, HttpServletRequest httpRequest) {
        String hash = refreshTokenService.hash(req.refreshToken());
        UserSession session = sessionRepository.findByRefreshTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Session expired — please log in again"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Session expired — please log in again");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Rotate the refresh token (revoke old, issue new) to limit replay if one leaks.
        session.setRevoked(true);
        sessionRepository.save(session);

        return issueSession(user, false, session.getDeviceId(), session.getDeviceName(), httpRequest);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = refreshTokenService.hash(rawRefreshToken);
        sessionRepository.findByRefreshTokenHashAndRevokedFalse(hash).ifPresent(session -> {
            session.setRevoked(true);
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void logoutAllDevices(UUID userId) {
        sessionRepository.findByUserIdAndRevokedFalse(userId).forEach(s -> s.setRevoked(true));
    }

    private AuthResponse issueSession(User user, boolean isNewUser, String deviceId, String deviceName,
                                       HttpServletRequest httpRequest) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getPhoneNumber());
        String rawRefreshToken = refreshTokenService.generateRawToken();

        UserSession session = UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(refreshTokenService.hash(rawRefreshToken))
                .deviceId(deviceId)
                .deviceName(deviceName)
                .userAgent(httpRequest.getHeader("User-Agent"))
                .ipAddress(httpRequest.getRemoteAddr())
                .expiresAt(Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS))
                .build();
        sessionRepository.save(session);

        UserDto dto = new UserDto(
                user.getId().toString(),
                user.getPhoneNumber(),
                user.getName(),
                user.getUsername(),
                user.getAbout(),
                user.getProfilePhotoUrl(),
                user.getLanguage()
        );

        return new AuthResponse(accessToken, rawRefreshToken, isNewUser, dto);
    }
}
