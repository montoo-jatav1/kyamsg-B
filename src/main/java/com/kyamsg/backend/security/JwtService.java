package com.kyamsg.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenTtlMinutes;

    public JwtService(
            @Value("${kyamsg.jwt.secret}") String secret,
            @Value("${kyamsg.jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes
    ) {
        // HS256 requires a key of at least 256 bits (32 bytes) — enforced at startup so a
        // weak/missing JWT_SECRET env var fails fast instead of silently producing forgeable tokens.
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalStateException(
                    "kyamsg.jwt.secret (JWT_SECRET env var) must be set and at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public String generateAccessToken(UUID userId, String phoneNumber) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("phone", phoneNumber)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Returns the subject (user id) if the token is valid and unexpired, otherwise throws. */
    public UUID validateAndGetUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }
}
