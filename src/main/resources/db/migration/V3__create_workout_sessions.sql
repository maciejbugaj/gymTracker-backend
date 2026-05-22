CREATE TABLE workout_sessions (
id BIGSERIAL PRIMARY KEY,
template_id BIGINT REFERENCES workout_templates(id) ON DELETE SET NULL,
started_at TIMESTAMP NOT NULL DEFAULT NOW(),
ended_at TIMESTAMP,
duration_seconds INT,
notes TEXT
);
