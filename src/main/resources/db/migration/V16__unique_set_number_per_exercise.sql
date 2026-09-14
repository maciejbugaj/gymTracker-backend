-- A checkbox per set row means the client can fire two POSTs for the same row (double tap,
-- slow network, uncheck/recheck). Without this index that lands two logs with the same
-- set_number, which breaks both the checkbox state and the tonnage totals in history.

-- Existing duplicates (if any) would block the index — keep the earliest row of each group.
DELETE FROM exercise_logs e
USING exercise_logs dup
WHERE e.session_id = dup.session_id
  AND e.exercise_name = dup.exercise_name
  AND e.set_number = dup.set_number
  AND e.id > dup.id;

CREATE UNIQUE INDEX uq_exercise_logs_session_exercise_set
    ON exercise_logs (session_id, exercise_name, set_number);
