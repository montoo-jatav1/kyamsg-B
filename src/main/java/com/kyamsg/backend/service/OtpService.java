package com.kyamsg.backend.service;

import com.kyamsg.backend.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final StringRedisTemplate redis;
    private final OtpSender otpSender;
    private final SecureRandom secureRandom = new SecureRandom();

    private String otpKey(String phone) { return "otp:code:" + phone; }
    private String cooldownKey(String phone) { return "otp:cooldown:" + phone; }
    private String attemptsKey(String phone) { return "otp:attempts:" + phone; }

    public void sendOtp(String phoneNumber) {
        Boolean onCooldown = redis.hasKey(cooldownKey(phoneNumber));
        if (Boolean.TRUE.equals(onCooldown)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another code");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        String hashed = hash(otp);

        redis.opsForValue().set(otpKey(phoneNumber), hashed, OTP_TTL);
        redis.opsForValue().set(cooldownKey(phoneNumber), "1", RESEND_COOLDOWN);
        redis.delete(attemptsKey(phoneNumber));

        otpSender.send(phoneNumber, otp);
    }

    public void verifyOtp(String phoneNumber, String candidateOtp) {
        String storedHash = redis.opsForValue().get(otpKey(phoneNumber));
        if (storedHash == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Code expired or not requested — request a new one");
        }

        Long attempts = redis.opsForValue().increment(attemptsKey(phoneNumber));
        redis.expire(attemptsKey(phoneNumber), OTP_TTL.toSeconds(), TimeUnit.SECONDS);
        if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
            redis.delete(otpKey(phoneNumber));
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts — request a new code");
        }

        if (!storedHash.equals(hash(candidateOtp))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect code");
        }

        // Correct — consume it so it can't be replayed.
        redis.delete(otpKey(phoneNumber));
        redis.delete(attemptsKey(phoneNumber));
    }

    private String hash(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(otp.getBytes()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
