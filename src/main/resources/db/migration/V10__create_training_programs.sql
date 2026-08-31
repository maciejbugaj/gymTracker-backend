CREATE TABLE training_programs (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  goal VARCHAR(40),
  experience_level VARCHAR(20),
  duration_weeks INT,
  days_per_week INT,
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  source VARCHAR(20) NOT NULL DEFAULT 'AI_GENERATED',
  ai_generation_id BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);