CREATE TABLE ai_plan_generations (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  status VARCHAR(20) NOT NULL,
  request_json JSONB,
  history_summary_json JSONB,
  model VARCHAR(40),
  input_tokens INT,
  output_tokens INT,
  raw_response TEXT,
  error_message TEXT,
  training_program_id BIGINT REFERENCES training_programs(id) ON DELETE SET NULL,
  previous_generation_id BIGINT REFERENCES ai_plan_generations(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
