-- Seed test organization and users for local development
-- Credentials: kari@everestsushi.no/admin123, ola@everestsushi.no/leder123, per@everestsushi.no/ansatt123

INSERT INTO organizations (id, name, org_number, created_at)
VALUES (1, 'Everest Sushi & Fusion AS', '937219997', CURRENT_TIMESTAMP);

INSERT INTO locations (id, organization_id, name, address, created_at)
VALUES (1, 1, 'Innherredsveien 1', 'Innherredsveien 1, 7014 Trondheim', CURRENT_TIMESTAMP);

INSERT INTO users (id, organization_id, home_location_id, email, first_name, last_name, password_hash, role, is_active, created_at)
VALUES
  (1, 1, 1, 'kari@everestsushi.no', 'Kari', 'Larsen',    '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'ADMIN',   TRUE, CURRENT_TIMESTAMP),
  (2, 1, 1, 'ola@everestsushi.no',  'Ola',  'Nordmann',  '$2a$10$hmxwibCoyoYBL4FqNv/lu.0EHEoumucw8W1Tq1gEDMztNhJnDX3hG', 'MANAGER', TRUE, CURRENT_TIMESTAMP),
  (3, 1, 1, 'per@everestsushi.no',  'Per',  'Martinsen', '$2a$10$71.VdovAzTx6gJxwS7QSXO4WxcYTH7NzmdnBQhvjVPgPuEUNMSj9W', 'STAFF',   TRUE, CURRENT_TIMESTAMP);
