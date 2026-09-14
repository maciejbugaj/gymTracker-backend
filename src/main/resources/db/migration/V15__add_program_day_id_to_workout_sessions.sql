ALTER TABLE workout_sessions
ADD program_day_id BIGINT REFERENCES program_days(id) ON DELETE SET NULL;
