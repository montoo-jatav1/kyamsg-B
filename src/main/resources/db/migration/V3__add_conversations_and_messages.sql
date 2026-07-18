CREATE TABLE conversations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_one_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_two_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_message_at     TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_conversations_pair UNIQUE (user_one_id, user_two_id),
    CONSTRAINT chk_conversations_ordered CHECK (user_one_id < user_two_id)
);

CREATE INDEX idx_conversations_user_one ON conversations (user_one_id);
CREATE INDEX idx_conversations_user_two ON conversations (user_two_id);

CREATE TABLE messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content             TEXT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SENT',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, created_at);
