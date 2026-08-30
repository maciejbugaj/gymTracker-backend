ALTER TABLE workout_templates
ADD user_id BIGINT REFERENCES users(id);

ALTER TABLE workout_sessions
ADD user_id BIGINT REFERENCES users(id);

INSERT INTO users (keycloak_id, email)
VALUES (gen_random_uuid(), 'example@luz.com');

UPDATE workout_templates
SET user_id = (SELECT id FROM users LIMIT 1);

ALTER TABLE workout_templates
ALTER COLUMN user_id SET NOT NULL;

UPDATE workout_sessions
SET user_id = (SELECT id FROM users LIMIT 1);

ALTER TABLE workout_sessions
ALTER COLUMN user_id SET NOT NULL;