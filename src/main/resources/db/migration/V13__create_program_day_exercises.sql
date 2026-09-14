CREATE TABLE program_day_exercises (
  id BIGSERIAL PRIMARY KEY,
  program_day_id BIGINT NOT NULL REFERENCES program_days(id) ON DELETE CASCADE,
  exercise_name VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  target_sets INT,
  target_reps_min INT,
  target_reps_max INT,
  rest_seconds INT,
  notes TEXT
);
