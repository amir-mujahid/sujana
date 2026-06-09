CREATE TABLE IF NOT EXISTS notifications (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category   TEXT        NOT NULL,
    title      TEXT        NOT NULL,
    body       TEXT        NOT NULL,
    deeplink   TEXT,
    data_json  TEXT,
    read_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS notification_prefs (
    user_id  UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category TEXT    NOT NULL,
    muted    BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (user_id, category)
);

CREATE TABLE IF NOT EXISTS device_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      TEXT        NOT NULL,
    platform   TEXT        NOT NULL DEFAULT 'android',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (token)
);

CREATE INDEX idx_device_tokens_user ON device_tokens(user_id);
