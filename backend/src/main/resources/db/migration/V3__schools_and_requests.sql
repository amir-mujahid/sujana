-- Stage 2: minimal schools table + requests table.
-- Schools enriched in Stage 7; minimal here for request dropoff_school_id FK.

CREATE TABLE IF NOT EXISTS schools (
    id          UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT             NOT NULL,
    lat         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    lng         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    tenant_id   UUID             NULL,
    created_at  TIMESTAMPTZ      NOT NULL DEFAULT now()
);

INSERT INTO schools (id, name, lat, lng) VALUES
    ('a1b2c3d4-0000-0000-0000-000000000001', 'SK Damansara Jaya',  3.1234,  101.6234),
    ('a1b2c3d4-0000-0000-0000-000000000002', 'SMK Subang Jaya',    3.0809,  101.5831),
    ('a1b2c3d4-0000-0000-0000-000000000003', 'SK Ara Damansara',   3.1109,  101.5771),
    ('a1b2c3d4-0000-0000-0000-000000000004', 'SK Taman Megah',     3.1012,  101.6104),
    ('a1b2c3d4-0000-0000-0000-000000000005', 'SMK Tropicana',      3.1523,  101.5887)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS requests (
    id                UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    type              TEXT             NOT NULL DEFAULT 'CONTRIBUTOR',
    requester_id      UUID             NOT NULL REFERENCES users(id),
    status            TEXT             NOT NULL DEFAULT 'PENDING',
    pickup_lat        DOUBLE PRECISION NOT NULL,
    pickup_lng        DOUBLE PRECISION NOT NULL,
    pickup_address    TEXT             NOT NULL DEFAULT '',
    dropoff_school_id UUID             NULL REFERENCES schools(id),
    notes             TEXT             NULL,
    photo_url         TEXT             NULL,
    created_at        TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_status       ON requests(status);
