-- Stage 1: Users table.
-- tenant_id is nullable here; FK to tenants table added in Stage 7.
CREATE TABLE IF NOT EXISTS users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid TEXT       NOT NULL UNIQUE,
    name        TEXT        NOT NULL,
    email       TEXT        NOT NULL,
    role        TEXT        NOT NULL DEFAULT 'CONTRIBUTOR',
    tenant_id   UUID        NULL,
    phone       TEXT        NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
