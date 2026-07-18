CREATE TABLE contacts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    contact_user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    saved_name          VARCHAR(80) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_contacts_owner_contact UNIQUE (owner_id, contact_user_id)
);

CREATE INDEX idx_contacts_owner ON contacts (owner_id);
CREATE INDEX idx_contacts_contact_user ON contacts (contact_user_id);
