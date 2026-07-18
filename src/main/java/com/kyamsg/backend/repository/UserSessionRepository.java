package com.kyamsg.backend.repository;

import com.kyamsg.backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findByRefreshTokenHashAndRevokedFalse(String refreshTokenHash);
    List<UserSession> findByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, Instant now);
    List<UserSession> findByUserIdAndRevokedFalse(UUID userId);
}
