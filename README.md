# SandeshX Backend (Ktor)

## Stack
Kotlin + Ktor · PostgreSQL (via Exposed) · Redis · MinIO · Firebase Cloud Messaging

## Local dev
```
cp .env.example .env   # fill in values, especially JWT_SECRET (openssl rand -hex 32)
docker compose up
```
Health check: `GET http://localhost:8080/health`

## Security — what's actually implemented
- **OTP**: random 6-digit code, BCrypt-hashed before storing in Redis, 5-minute TTL, max 5 verify
  attempts, max 5 sends/hour per phone number. **No bypass exists anywhere in this codebase.**
- **JWT**: short-lived access token (15 min) + refresh token (30 days), HMAC256, secret loaded only
  from `JWT_SECRET` env var — the app refuses to boot without it.
- **Passwords/OTP**: BCrypt, never stored or logged in plaintext.
- **Images**: uploaded directly from the phone to MinIO via a presigned URL — the backend never
  receives or stores raw image bytes.
- **Rate limiting**: global 60 req/min per client via Ktor's RateLimit plugin, plus the
  OTP-specific limits above.
- **CORS**: locked to `ALLOWED_ORIGIN` in production; wildcard is a dev-only fallback.

## Before going to production
1. Set a real `JWT_SECRET`, `DATABASE_PASSWORD`, `MINIO_SECRET_KEY` — none of the example values.
2. Replace `LogSmsSender` (`services/SmsSender.kt`) with a real provider (Twilio, MSG91, etc).
3. Set `ALLOWED_ORIGIN` to your real domain; remove the `anyHost()` dev fallback in `Application.kt`.
4. Put this behind HTTPS/TLS (a reverse proxy like Caddy or an ALB) — JWTs over plain HTTP leak.
5. Set `FIREBASE_CREDENTIALS_PATH` to a service-account JSON from your Firebase project to enable
   push notifications for offline users.
6. If you scale to multiple backend instances, back `ConnectionRegistry` with Redis Pub/Sub so a
   message can reach a recipient connected to a different instance.

## API surface
- `POST /api/auth/otp/send` `{phoneNumber}`
- `POST /api/auth/otp/verify` `{phoneNumber, code}` → `{accessToken, refreshToken, isNewUser, userId}`
- `GET  /api/users/me` (JWT)
- `GET  /api/users/{id}` (JWT)
- `POST /api/users/me/fcm-token` `{token}` (JWT)
- `GET  /api/chats/{userId}/messages?before=` (JWT)
- `POST /api/chats/{userId}/read/{messageId}` (JWT)
- `POST /api/media/upload-url` (JWT) → presigned MinIO PUT/GET URLs
- `WS   /ws/chat?token=<accessToken>` — realtime message/read/typing events
