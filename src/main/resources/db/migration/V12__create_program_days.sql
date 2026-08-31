CREATE TABLE program_days (
  id BIGSERIAL PRIMARY KEY,
  program_week_id BIGINT NOT NULL REFERENCES program_weeks(id) ON DELETE CASCADE,
  day_number INT NOT NULL,
  name VARCHAR(255),
  notes TEXT
);
