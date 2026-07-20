package com.sandeshx.services

interface SmsSender {
    fun send(phoneNumber: String, code: String)
}

/** Dev-only: logs the code instead of sending a real SMS. Replace with Twilio/MSG91 in production. */
class LogSmsSender : SmsSender {
    override fun send(phoneNumber: String, code: String) {
        println("[DEV SMS] OTP for $phoneNumber -> $code (wire up a real provider before shipping)")
    }
}
