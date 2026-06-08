-- Stage 0 baseline migration. Confirms DB connectivity; real tables added from Stage 1.
CREATE TABLE IF NOT EXISTS _sujana_meta (
    key   TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

INSERT INTO _sujana_meta (key, value)
VALUES ('schema_version', '0')
ON CONFLICT (key) DO NOTHING;
