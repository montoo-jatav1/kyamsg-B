package com.kyamsg.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * DEVELOPMENT-ONLY OTP delivery. No SMS provider is configured yet, so the
 * code is written to the server log instead of being texted to the phone.
 *
 * This exists so the rest of the auth flow (generation, hashing, expiry,
 * rate-limiting, verification) is fully real and testable end-to-end right
 * now, without needing a paid SMS account first.
 *
 * TO GO LIVE: sign up with an SMS provider (Twilio, MSG91, etc.), implement
 * OtpSender against their API in a new @Component (e.g. TwilioOtpSender),
 * and remove the @ConditionalOnMissingBean here (or delete this class) so
 * yours is the one Spring wires up.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(name = "productionOtpSender")
public class LogOtpSender implements OtpSender {
    @Override
    public void send(String phoneNumber, String otp) {
        log.warn("[DEV OTP — no SMS provider configured] {} -> code {}", phoneNumber, otp);
    }
}
