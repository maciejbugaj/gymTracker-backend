CREATE TABLE template_exercises (
id BIGSERIAL PRIMARY KEY,
template_id BIGINT REFERENCES workout_templates(id) ON DELETE CASCADE,
exercise_name VARCHAR(255) NOT NULL,
default_sets INT,
default_reps INT,
default_weight_kg DECIMAL(6,2),
sort_order INT NOT NULL DEFAULT 0
);

