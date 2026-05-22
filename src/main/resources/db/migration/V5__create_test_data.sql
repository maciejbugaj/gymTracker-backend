INSERT INTO workout_templates (name, description) VALUES
    ('Session A', 'Push day - chest, shoulders and triceps'),
    ('Session B', 'Pull day - back and biceps'),
    ('Session C', 'Leg day - quads, hamstrings and glutes');

INSERT INTO template_exercises (template_id, exercise_name, default_sets, default_reps, default_weight_kg, sort_order) VALUES
    (1, 'Bench press',        4, 5,  80.00, 1),
    (1, 'Overhead press',     3, 8,  50.00, 2),
    (1, 'Incline dumbbell',   3, 10, 30.00, 3),
    (1, 'Tricep pushdown',    3, 12, 20.00, 4),

    (2, 'Deadlift',           4, 5,  100.00, 1),
    (2, 'Barbell row',        4, 6,  70.00,  2),
    (2, 'Pull ups',           3, 8,  NULL,   3),
    (2, 'Barbell curl',       3, 10, 30.00,  4),
    (2, 'Face pulls',         3, 15, 15.00,  5),

    (3, 'Squat',              4, 5,  90.00, 1),
    (3, 'Romanian deadlift',  3, 8,  70.00, 2),
    (3, 'Leg press',          3, 10, 120.00, 3),
    (3, 'Leg curl',           3, 12, 40.00, 4),
    (3, 'Calf raises',        4, 15, 60.00, 5);

INSERT INTO workout_sessions (template_id, started_at, ended_at, duration_seconds, notes) VALUES
    (1, '2024-01-15 09:00:00', '2024-01-15 10:05:00', 3900, 'Good session, bench felt strong'),
    (2, '2024-01-17 09:00:00', '2024-01-17 10:10:00', 4200, 'Deadlift PR today - 110kg'),
    (3, '2024-01-19 09:15:00', '2024-01-19 10:20:00', 3900, 'Legs were sore from last week'),
    (1, '2024-01-22 09:00:00', '2024-01-22 10:00:00', 3600, 'Increased bench to 82.5kg'),
    (2, '2024-01-24 09:00:00', '2024-01-24 10:15:00', 4500, NULL);

INSERT INTO exercise_logs (session_id, exercise_name, set_number, reps, weight_kg, logged_at) VALUES
    (1, 'Bench press',    1, 5, 80.00, '2024-01-15 09:05:00'),
    (1, 'Bench press',    2, 5, 80.00, '2024-01-15 09:08:00'),
    (1, 'Bench press',    3, 5, 80.00, '2024-01-15 09:11:00'),
    (1, 'Bench press',    4, 4, 80.00, '2024-01-15 09:14:00'),
    (1, 'Overhead press', 1, 8, 50.00, '2024-01-15 09:25:00'),
    (1, 'Overhead press', 2, 8, 50.00, '2024-01-15 09:28:00'),
    (1, 'Overhead press', 3, 7, 50.00, '2024-01-15 09:31:00'),
    (1, 'Incline dumbbell', 1, 10, 30.00, '2024-01-15 09:42:00'),
    (1, 'Incline dumbbell', 2, 10, 30.00, '2024-01-15 09:46:00'),
    (1, 'Incline dumbbell', 3, 9,  30.00, '2024-01-15 09:50:00'),

    (2, 'Deadlift',    1, 5, 100.00, '2024-01-17 09:05:00'),
    (2, 'Deadlift',    2, 5, 100.00, '2024-01-17 09:10:00'),
    (2, 'Deadlift',    3, 5, 105.00, '2024-01-17 09:15:00'),
    (2, 'Deadlift',    4, 3, 110.00, '2024-01-17 09:20:00'),
    (2, 'Barbell row', 1, 6, 70.00,  '2024-01-17 09:35:00'),
    (2, 'Barbell row', 2, 6, 70.00,  '2024-01-17 09:39:00'),
    (2, 'Barbell row', 3, 6, 70.00,  '2024-01-17 09:43:00'),
    (2, 'Pull ups',    1, 8, NULL,   '2024-01-17 09:55:00'),
    (2, 'Pull ups',    2, 7, NULL,   '2024-01-17 09:59:00'),
    (2, 'Pull ups',    3, 6, NULL,   '2024-01-17 10:03:00'),

    (3, 'Squat',             1, 5, 90.00,  '2024-01-19 09:20:00'),
    (3, 'Squat',             2, 5, 90.00,  '2024-01-19 09:25:00'),
    (3, 'Squat',             3, 5, 90.00,  '2024-01-19 09:30:00'),
    (3, 'Squat',             4, 4, 90.00,  '2024-01-19 09:35:00'),
    (3, 'Romanian deadlift', 1, 8, 70.00,  '2024-01-19 09:48:00'),
    (3, 'Romanian deadlift', 2, 8, 70.00,  '2024-01-19 09:52:00'),
    (3, 'Romanian deadlift', 3, 7, 70.00,  '2024-01-19 09:56:00'),
    (3, 'Leg press',         1, 10, 120.00, '2024-01-19 10:06:00'),
    (3, 'Leg press',         2, 10, 120.00, '2024-01-19 10:10:00'),
    (3, 'Leg press',         3, 9,  120.00, '2024-01-19 10:14:00'),

    (4, 'Bench press',    1, 5, 82.50, '2024-01-22 09:05:00'),
    (4, 'Bench press',    2, 5, 82.50, '2024-01-22 09:08:00'),
    (4, 'Bench press',    3, 5, 82.50, '2024-01-22 09:11:00'),
    (4, 'Bench press',    4, 5, 82.50, '2024-01-22 09:14:00'),
    (4, 'Overhead press', 1, 8, 52.50, '2024-01-22 09:26:00'),
    (4, 'Overhead press', 2, 8, 52.50, '2024-01-22 09:29:00'),
    (4, 'Overhead press', 3, 8, 52.50, '2024-01-22 09:32:00'),

    (5, 'Deadlift',    1, 5, 105.00, '2024-01-24 09:05:00'),
    (5, 'Deadlift',    2, 5, 105.00, '2024-01-24 09:10:00'),
    (5, 'Deadlift',    3, 5, 105.00, '2024-01-24 09:15:00'),
    (5, 'Deadlift',    4, 5, 105.00, '2024-01-24 09:20:00'),
    (5, 'Barbell row', 1, 6, 72.50,  '2024-01-24 09:35:00'),
    (5, 'Barbell row', 2, 6, 72.50,  '2024-01-24 09:39:00'),
    (5, 'Barbell row', 3, 6, 72.50,  '2024-01-24 09:43:00'),
    (5, 'Barbell row', 4, 5, 72.50,  '2024-01-24 09:47:00');