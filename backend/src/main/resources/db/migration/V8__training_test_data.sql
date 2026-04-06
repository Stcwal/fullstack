INSERT INTO training_records (user_id, training_type, status, completed_at, expires_at)
SELECT u.id, 'GENERAL', 'COMPLETED', CURRENT_TIMESTAMP, NULL
FROM users u
WHERE u.email = 'admin@everest.no'
  AND NOT EXISTS (
      SELECT 1
      FROM training_records tr
      WHERE tr.user_id = u.id
        AND tr.training_type = 'GENERAL'
  );

INSERT INTO training_records (user_id, training_type, status, completed_at, expires_at)
SELECT u.id, 'CHECKLIST_APPROVAL', 'COMPLETED', CURRENT_TIMESTAMP, TIMESTAMPADD(DAY, 365, CURRENT_TIMESTAMP)
FROM users u
WHERE u.email = 'manager@everest.no'
  AND NOT EXISTS (
      SELECT 1
      FROM training_records tr
      WHERE tr.user_id = u.id
        AND tr.training_type = 'CHECKLIST_APPROVAL'
  );

INSERT INTO training_records (user_id, training_type, status, completed_at, expires_at)
SELECT u.id, 'FREEZER_LOGGING', 'COMPLETED', CURRENT_TIMESTAMP, TIMESTAMPADD(DAY, 180, CURRENT_TIMESTAMP)
FROM users u
WHERE u.email = 'staff@everest.no'
  AND NOT EXISTS (
      SELECT 1
      FROM training_records tr
      WHERE tr.user_id = u.id
        AND tr.training_type = 'FREEZER_LOGGING'
  );
