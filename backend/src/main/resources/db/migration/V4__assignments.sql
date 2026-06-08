CREATE TABLE assignments (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id    UUID        NOT NULL REFERENCES requests(id),
    rider_id      UUID        NOT NULL REFERENCES users(id),
    dispatcher_id UUID        REFERENCES users(id),
    status        TEXT        NOT NULL DEFAULT 'ASSIGNED',
    assigned_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at   TIMESTAMPTZ,
    collected_at  TIMESTAMPTZ,
    delivered_at  TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ
);

-- Only one active (non-cancelled) assignment per request at a time
CREATE UNIQUE INDEX idx_assignments_active_request
    ON assignments(request_id)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_assignments_rider_id ON assignments(rider_id);
CREATE INDEX idx_assignments_status   ON assignments(status);
