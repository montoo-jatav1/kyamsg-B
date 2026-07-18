package com.kyamsg.backend.service;

/**
 * Abstraction over however OTPs actually get delivered to a phone.
 * The OTP generation/hashing/expiry/rate-limiting logic in OtpService is fully
 * real and production-ready regardless of which implementation of this
 * interface is wired up.
 *
 * Swap {@link LogOtpSender} for a real implementation (Twilio, MSG91, etc.)
 * once you have an SMS provider account — see LogOtpSender's javadoc.
 */
public interface OtpSender {
    void send(String phoneNumber, String otp);
}
