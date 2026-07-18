CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number             VARCHAR(20) NOT NULL,
    name                     VARCHAR(80),
    username                 VARCHAR(40),
    about                    VARCHAR(160),
    profile_photo_url        TEXT,
    recovery_email           VARCHAR(255),
    language                 VARCHAR(10)  NOT NULL DEFAULT 'en',
    last_seen                TIMESTAMPTZ,
    is_online                BOOLEAN      NOT NULL DEFAULT false,
    last_seen_privacy        VARCHAR(20)  NOT NULL DEFAULT 'everyone',
    profile_photo_privacy    VARCHAR(20)  NOT NULL DEFAULT 'everyone',
    about_privacy            VARCHAR(20)  NOT NULL DEFAULT 'everyone',
    read_receipts_enabled    BOOLEAN      NOT NULL DEFAULT true,
    myra_ai_key_encrypted    TEXT,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_users_phone ON users (phone_number);
CREATE UNIQUE INDEX idx_users_username ON users (username) WHERE username IS NOT NULL;

CREATE TABLE user_sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash  VARCHAR(255) NOT NULL,
    device_name         VARCHAR(120),
    device_id           VARCHAR(120),
    user_agent          TEXT,
    ip_address          VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked             BOOLEAN NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX idx_sessions_refresh_hash ON user_sessions (refresh_token_hash);
CREATE INDEX idx_sessions_user ON user_sessions (user_id);
