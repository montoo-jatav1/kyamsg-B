# KyaMsg Backend — v2 (Spring Boot + PostgreSQL + Redis)

This is a from-scratch rewrite, replacing the old H2/file-based backend.

## Phase 1 (this delivery) — Authentication, real and complete

- Phone number + OTP login (OTP hashed, 5-min expiry, 60-sec resend cooldown, 5 max attempts — all enforced in Redis)
- JWT access tokens (15 min) + rotating opaque refresh tokens (30 days, SHA-256 hashed at rest)
- Multi-device sessions (`user_sessions` table) with logout / logout-all-devices
- PostgreSQL schema via Flyway migrations (no `ddl-auto: update` — real migrations only)
- Dockerfile + docker-compose (Postgres + Redis + backend) for one-command local run
- Render-ready: all config comes from environment variables

### Not yet built (coming in the next phases, in priority order)
1. Profile setup (name/username/photo/recovery email) + Chats/Messages/WebSocket
2. Groups, Calls, Status
3. Media upload/compression, notifications (FCM)
4. Admin dashboard
5. Myra AI (Gemini-backed) settings screen + chat integration

## One important honesty note about OTP delivery

There's no SMS provider account connected yet (Twilio, MSG91, etc. all cost money and
need your own account). Right now, `LogOtpSender` **writes the OTP to the server log**
instead of texting it — this is clearly a development stand-in, not hidden anywhere.
Everything else about OTP (generation, hashing, expiry, rate-limiting, verification) is
fully real. When you're ready to send real SMS, implement `OtpSender` against your
provider's API (one small class) — see the javadoc in `LogOtpSender.java`.

## Running locally

```bash
cp .env.example .env
# edit .env and set JWT_SECRET (openssl rand -base64 48)
docker-compose up --build
```

Backend will be up at `http://localhost:8080`. Health check: `http://localhost:8080/actuator/health`.
API docs: `http://localhost:8080/swagger-ui.html`.

## Endpoints in this phase

| Method | Path                  | Auth required | Description                          |
|--------|-----------------------|----------------|---------------------------------------|
| POST   | /api/auth/otp/send    | No             | `{ "phoneNumber": "+919876543210" }`  |
| POST   | /api/auth/otp/verify  | No             | `{ "phoneNumber", "otp", "deviceId", "deviceName" }` → tokens + user |
| POST   | /api/auth/refresh     | No             | `{ "refreshToken" }` → new token pair |
| POST   | /api/auth/logout      | No             | `{ "refreshToken" }` → revokes that session |
| POST   | /api/auth/logout-all  | Yes (Bearer)   | Revokes every session for the caller  |

## A note on verification

I don't have a JDK compiler, Maven, or internet access in the sandbox I built this in,
so I could not actually run `mvn compile` here — I reviewed every file carefully by
hand instead. Please run `docker-compose up --build` on your machine as the real test;
if anything fails to compile or run, paste the exact error and I'll fix it immediately.
