CREATE TABLE exercise_logs (
id BIGSERIAL PRIMARY KEY,
session_id BIGINT REFERENCES workout_sessions(id) ON DELETE CASCADE,
exercise_name VARCHAR(255) NOT NULL,
set_number INT NOT NULL,
reps INT,
weight_kg DECIMAL(6,2),
logged_at TIMESTAMP NOT NULL DEFAULT NOW()
);