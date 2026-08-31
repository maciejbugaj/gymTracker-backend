CREATE TABLE program_weeks (
  id BIGSERIAL PRIMARY KEY,
  program_id BIGINT NOT NULL REFERENCES training_programs(id) ON DELETE CASCADE,
  week_number INT NOT NULL,
  focus VARCHAR(40),
  is_deload BOOLEAN NOT NULL DEFAULT FALSE,
  notes TEXT
);
