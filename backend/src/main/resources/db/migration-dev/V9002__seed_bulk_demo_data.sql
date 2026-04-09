-- Bulk demo data for local/dev manual testing.
-- Depends on V9000/V9001 dev seeds (org 1 + core users).

-- ---------------------------------------------------------------------------
-- Extra locations and users
-- ---------------------------------------------------------------------------

INSERT INTO locations (id, organization_id, name, address, created_at)
VALUES
  (2, 1, 'Munkegata 14', 'Munkegata 14, 7011 Trondheim', CURRENT_TIMESTAMP),
  (3, 1, 'Nedre Elvehavn 5', 'Nedre Elvehavn 5, 7014 Trondheim', CURRENT_TIMESTAMP),
  (4, 1, 'Solsiden Brygge 3', 'Solsiden Brygge 3, 7014 Trondheim', CURRENT_TIMESTAMP),
  (5, 1, 'Bakklandet 22', 'Bakklandet 22, 7013 Trondheim', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address);

-- Password hash below is the same as admin123 in V9001 for easier local testing.
INSERT INTO users (id, organization_id, home_location_id, email, first_name, last_name, password_hash, role, is_active, created_at)
VALUES
  (4, 1, 2, 'lina@everestsushi.no',   'Lina',   'Dahl',      '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'MANAGER', TRUE, CURRENT_TIMESTAMP),
  (5, 1, 2, 'jon@everestsushi.no',    'Jon',    'Berg',      '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'STAFF',   TRUE, CURRENT_TIMESTAMP),
  (6, 1, 3, 'sara@everestsushi.no',   'Sara',   'Nilsen',    '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'STAFF',   TRUE, CURRENT_TIMESTAMP),
  (7, 1, 3, 'mats@everestsushi.no',   'Mats',   'Andreassen','$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'STAFF',   TRUE, CURRENT_TIMESTAMP),
  (8, 1, 4, 'ingrid@everestsushi.no', 'Ingrid', 'Hauge',     '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'MANAGER', TRUE, CURRENT_TIMESTAMP),
  (9, 1, 4, 'emil@everestsushi.no',   'Emil',   'Lunde',     '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'STAFF',   TRUE, CURRENT_TIMESTAMP),
  (10,1, 5, 'noah@everestsushi.no',   'Noah',   'Moen',      '$2a$10$rRubbwtleDuLaReYZClVcOZw9402f0TGEVJfWTjCNt935tUMwkFWm', 'STAFF',   TRUE, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE email = VALUES(email), home_location_id = VALUES(home_location_id), role = VALUES(role);

INSERT INTO user_locations (user_id, location_id)
VALUES
  (2, 1), (2, 2), (2, 3),
  (4, 2), (4, 3),
  (8, 4), (8, 5),
  (5, 2), (6, 3), (7, 3), (9, 4), (10, 5)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO user_location_scope_assignments
  (id, user_id, location_id, starts_at, ends_at, assignment_mode, status, completed_at, confirmed_at, reason, created_at)
VALUES
  (9001, 5, 2, TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), NULL, 'INHERIT', 'ACTIVE', NULL, NULL, 'Default assignment', CURRENT_TIMESTAMP),
  (9002, 6, 3, TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), NULL, 'INHERIT', 'ACTIVE', NULL, NULL, 'Default assignment', CURRENT_TIMESTAMP),
  (9003, 7, 3, TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP), NULL, 'INHERIT', 'ACTIVE', NULL, NULL, 'Default assignment', CURRENT_TIMESTAMP),
  (9004, 9, 4, TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP), NULL, 'INHERIT', 'ACTIVE', NULL, NULL, 'Default assignment', CURRENT_TIMESTAMP),
  (9005, 10, 5, TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP), NULL, 'INHERIT', 'ACTIVE', NULL, NULL, 'Default assignment', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE status = VALUES(status), ends_at = VALUES(ends_at);

-- ---------------------------------------------------------------------------
-- Temperature units and bulk readings
-- ---------------------------------------------------------------------------

INSERT INTO temperature_units
  (id, organization_id, name, type, target_temperature, min_threshold, max_threshold, description, active, deleted_at, created_at, updated_at)
VALUES
  (5001, 1, 'Fryser A1', 'FREEZER', -18.0, -22.0, -15.0, 'Main freezer line A1', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5002, 1, 'Fryser A2', 'FREEZER', -18.0, -22.0, -15.0, 'Main freezer line A2', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5003, 1, 'Fryser B1', 'FREEZER', -18.0, -22.0, -15.0, 'Storage freezer B1', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5004, 1, 'Fryser B2', 'FREEZER', -18.0, -22.0, -15.0, 'Storage freezer B2', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5005, 1, 'Kjoleskap Sushibar 1', 'FRIDGE', 4.0, 1.0, 6.0, 'Cold prep station', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5006, 1, 'Kjoleskap Sushibar 2', 'FRIDGE', 4.0, 1.0, 6.0, 'Cold prep station', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5007, 1, 'Kjolerom Lager', 'COOLER', 4.0, 1.0, 6.0, 'Backroom cooler', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5008, 1, 'Kjolerom Drikke', 'COOLER', 5.0, 2.0, 8.0, 'Beverage cooler', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5009, 1, 'Utstilling Disk 1', 'DISPLAY', 6.0, 2.0, 8.0, 'Front display', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5010, 1, 'Utstilling Disk 2', 'DISPLAY', 6.0, 2.0, 8.0, 'Front display', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5011, 1, 'Sausstasjon', 'OTHER', 8.0, 4.0, 10.0, 'Prepared sauces', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5012, 1, 'Ravarer Kaldsone', 'OTHER', 5.0, 2.0, 7.0, 'Raw ingredients zone', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name), updated_at = CURRENT_TIMESTAMP;

WITH RECURSIVE seq AS (
  SELECT 0 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 191
)
INSERT INTO temperature_readings
  (id, organization_id, unit_id, recorded_by_user_id, temperature, recorded_at, note, is_deviation, created_at, updated_at)
SELECT
  7000 + n,
  1,
  5001 + MOD(n, 12),
  CASE MOD(n, 7)
    WHEN 0 THEN 1
    WHEN 1 THEN 2
    WHEN 2 THEN 3
    WHEN 3 THEN 4
    WHEN 4 THEN 5
    WHEN 5 THEN 6
    ELSE 8
  END,
  CASE
    WHEN MOD(n, 23) = 0 THEN -10.5
    WHEN MOD(n, 29) = 0 THEN 10.2
    ELSE CASE
      WHEN MOD(n, 12) IN (0, 1, 2, 3) THEN -18.4 + (MOD(n, 6) * 0.35)
      WHEN MOD(n, 12) IN (4, 5, 6, 7) THEN 3.4 + (MOD(n, 7) * 0.28)
      ELSE 5.2 + (MOD(n, 5) * 0.31)
    END
  END,
  TIMESTAMPADD(HOUR, -n, CURRENT_TIMESTAMP),
  CASE WHEN MOD(n, 23) = 0 OR MOD(n, 29) = 0 THEN 'Auto-generated deviation sample' ELSE NULL END,
  CASE WHEN MOD(n, 23) = 0 OR MOD(n, 29) = 0 THEN TRUE ELSE FALSE END,
  TIMESTAMPADD(HOUR, -n, CURRENT_TIMESTAMP),
  TIMESTAMPADD(HOUR, -n, CURRENT_TIMESTAMP)
FROM seq
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- ---------------------------------------------------------------------------
-- Deviations and comments
-- ---------------------------------------------------------------------------

INSERT INTO deviations
  (id, organization_id, title, description, status, severity, module_type, reported_by_id, related_reading_id, resolved_by_id, resolved_at, resolution, created_at, updated_at)
VALUES
  (8001, 1, 'Fryser A1 over terskel', 'Malt temperatur var for hoy i 20 min.', 'OPEN',        'HIGH',     'IK_MAT',     2, 7023, NULL, NULL, NULL, TIMESTAMPADD(HOUR, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -30, CURRENT_TIMESTAMP)),
  (8002, 1, 'Kjolerom Drikke ustabil', 'Store variasjoner i temperatur siste dogn.', 'IN_PROGRESS', 'MEDIUM',   'IK_MAT',     4, 7046, NULL, NULL, NULL, TIMESTAMPADD(HOUR, -48, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -12, CURRENT_TIMESTAMP)),
  (8003, 1, 'Manglende alderskontroll', 'Gjesten ble servert uten gyldig legitimasjon.', 'RESOLVED',    'CRITICAL', 'IK_ALKOHOL', 8, NULL, 1, TIMESTAMPADD(HOUR, -18, CURRENT_TIMESTAMP), 'Personale fikk oppfriskningskurs og ny dobbelkontrollrutine.', TIMESTAMPADD(HOUR, -72, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -18, CURRENT_TIMESTAMP)),
  (8004, 1, 'Utstilling Disk 2 for varm', 'Visningsdisk holdt over 8C i rushperiode.', 'RESOLVED',    'HIGH',     'SHARED',     5, 7069, 4, TIMESTAMPADD(HOUR, -8, CURRENT_TIMESTAMP), 'Byttet vifte og justerte luftstrom.', TIMESTAMPADD(HOUR, -26, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -8, CURRENT_TIMESTAMP)),
  (8005, 1, 'Dokumentasjon ikke oppdatert', 'Siste internrevisjon mangler signatur.', 'OPEN',        'LOW',      'SHARED',     1, NULL, NULL, NULL, NULL, TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP)),
  (8006, 1, 'Underbemannet kveldsvakt', 'Kun en sertifisert personell i serveringssonen.', 'IN_PROGRESS', 'MEDIUM', 'IK_ALKOHOL', 2, NULL, NULL, NULL, NULL, TIMESTAMPADD(HOUR, -15, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -6, CURRENT_TIMESTAMP))
ON DUPLICATE KEY UPDATE status = VALUES(status), resolution = VALUES(resolution), updated_at = VALUES(updated_at);

INSERT INTO deviation_comments
  (id, organization_id, deviation_id, created_by_id, comment_text, created_at, updated_at)
VALUES
  (8101, 1, 8001, 2, 'Venter pa servicepartner for fysisk kontroll.', TIMESTAMPADD(HOUR, -28, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -28, CURRENT_TIMESTAMP)),
  (8102, 1, 8002, 4, 'La inn midlertidig kontroll hver 30. minutt.', TIMESTAMPADD(HOUR, -24, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -24, CURRENT_TIMESTAMP)),
  (8103, 1, 8003, 1, 'Gjennomfort samtale med involvert ansatt.', TIMESTAMPADD(HOUR, -20, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -20, CURRENT_TIMESTAMP)),
  (8104, 1, 8004, 4, 'Sjekket at ny vifte holder stabil temperatur.', TIMESTAMPADD(HOUR, -7, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -7, CURRENT_TIMESTAMP)),
  (8105, 1, 8006, 2, 'Oppdaterer vaktplan for kommende helg.', TIMESTAMPADD(HOUR, -5, CURRENT_TIMESTAMP), TIMESTAMPADD(HOUR, -5, CURRENT_TIMESTAMP))
ON DUPLICATE KEY UPDATE comment_text = VALUES(comment_text), updated_at = VALUES(updated_at);

-- ---------------------------------------------------------------------------
-- Checklists
-- ---------------------------------------------------------------------------

INSERT INTO checklist_templates (id, organization_id, title, frequency)
VALUES
  (4101, 1, 'Daglig aapning kjokken', 'DAILY'),
  (4102, 1, 'Daglig stenging kjokken', 'DAILY'),
  (4103, 1, 'Ukentlig renhold fryserom', 'WEEKLY'),
  (4104, 1, 'Maanedlig HACCP gjennomgang', 'MONTHLY'),
  (4105, 1, 'Daglig alderskontroll bar', 'DAILY')
ON DUPLICATE KEY UPDATE title = VALUES(title), frequency = VALUES(frequency);

INSERT INTO checklist_template_items (id, template_id, item_text)
VALUES
  (4201, 4101, 'Desinfiser alle arbeidsflater'),
  (4202, 4101, 'Kalibrer termometer'),
  (4203, 4101, 'Kontroller leveringstemperatur for fisk'),
  (4204, 4102, 'Logg slutt-temperatur pa alle enheter'),
  (4205, 4102, 'Lukk avvik i systemet'),
  (4206, 4103, 'Tin og rengjor fordamper'),
  (4207, 4103, 'Kontroller pakninger og dorlister'),
  (4208, 4104, 'Signer intern revisjonsprotokoll'),
  (4209, 4104, 'Oppdater risikovurdering'),
  (4210, 4105, 'Brief ansatte pa legitimasjonsrutiner'),
  (4211, 4105, 'Test avviksflyt for underaarige')
ON DUPLICATE KEY UPDATE item_text = VALUES(item_text);

INSERT INTO checklist_instances (id, template_id, organization_id, title, frequency, date, status)
VALUES
  (4301, 4101, 1, 'Daglig aapning kjokken', 'DAILY', CURRENT_DATE, 'IN_PROGRESS'),
  (4302, 4102, 1, 'Daglig stenging kjokken', 'DAILY', CURRENT_DATE, 'PENDING'),
  (4303, 4103, 1, 'Ukentlig renhold fryserom', 'WEEKLY', CURRENT_DATE, 'IN_PROGRESS'),
  (4304, 4105, 1, 'Daglig alderskontroll bar', 'DAILY', CURRENT_DATE, 'COMPLETED'),
  (4305, 4101, 1, 'Daglig aapning kjokken', 'DAILY', TIMESTAMPADD(DAY, -1, CURRENT_DATE), 'COMPLETED'),
  (4306, 4102, 1, 'Daglig stenging kjokken', 'DAILY', TIMESTAMPADD(DAY, -1, CURRENT_DATE), 'COMPLETED'),
  (4307, 4101, 1, 'Daglig aapning kjokken', 'DAILY', TIMESTAMPADD(DAY, -2, CURRENT_DATE), 'COMPLETED'),
  (4308, 4102, 1, 'Daglig stenging kjokken', 'DAILY', TIMESTAMPADD(DAY, -2, CURRENT_DATE), 'COMPLETED'),
  (4309, 4103, 1, 'Ukentlig renhold fryserom', 'WEEKLY', TIMESTAMPADD(DAY, -7, CURRENT_DATE), 'COMPLETED'),
  (4310, 4104, 1, 'Maanedlig HACCP gjennomgang', 'MONTHLY', TIMESTAMPADD(DAY, -14, CURRENT_DATE), 'IN_PROGRESS')
ON DUPLICATE KEY UPDATE status = VALUES(status), date = VALUES(date);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4401, 4301, 'Desinfiser alle arbeidsflater', TRUE, 3, TIMESTAMPADD(HOUR, -3, CURRENT_TIMESTAMP)),
  (4402, 4301, 'Kalibrer termometer', TRUE, 3, TIMESTAMPADD(HOUR, -2, CURRENT_TIMESTAMP)),
  (4403, 4301, 'Kontroller leveringstemperatur for fisk', FALSE, NULL, NULL),
  (4404, 4302, 'Logg slutt-temperatur pa alle enheter', FALSE, NULL, NULL),
  (4405, 4302, 'Lukk avvik i systemet', FALSE, NULL, NULL),
  (4406, 4303, 'Tin og rengjor fordamper', TRUE, 4, TIMESTAMPADD(HOUR, -8, CURRENT_TIMESTAMP)),
  (4407, 4303, 'Kontroller pakninger og dorlister', FALSE, NULL, NULL),
  (4408, 4304, 'Brief ansatte pa legitimasjonsrutiner', TRUE, 8, TIMESTAMPADD(HOUR, -9, CURRENT_TIMESTAMP)),
  (4409, 4304, 'Test avviksflyt for underaarige', TRUE, 8, TIMESTAMPADD(HOUR, -8, CURRENT_TIMESTAMP)),
  (4410, 4310, 'Signer intern revisjonsprotokoll', TRUE, 1, TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP)),
  (4411, 4310, 'Oppdater risikovurdering', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed), completed_by_user_id = VALUES(completed_by_user_id), completed_at = VALUES(completed_at);

-- ---------------------------------------------------------------------------
-- Training records
-- ---------------------------------------------------------------------------

INSERT INTO training_records (id, user_id, training_type, status, completed_at, expires_at)
VALUES
  (10001, 1, 'GENERAL', 'COMPLETED', TIMESTAMPADD(DAY, -200, CURRENT_TIMESTAMP), NULL),
  (10002, 2, 'CHECKLIST_APPROVAL', 'COMPLETED', TIMESTAMPADD(DAY, -120, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 245, CURRENT_TIMESTAMP)),
  (10003, 3, 'FREEZER_LOGGING', 'COMPLETED', TIMESTAMPADD(DAY, -60, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 120, CURRENT_TIMESTAMP)),
  (10004, 4, 'CHECKLIST_APPROVAL', 'COMPLETED', TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 330, CURRENT_TIMESTAMP)),
  (10005, 5, 'GENERAL', 'IN_PROGRESS', TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP), NULL),
  (10006, 6, 'FREEZER_LOGGING', 'COMPLETED', TIMESTAMPADD(DAY, -40, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 140, CURRENT_TIMESTAMP)),
  (10007, 7, 'FREEZER_LOGGING', 'EXPIRED', TIMESTAMPADD(DAY, -380, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP)),
  (10008, 8, 'CHECKLIST_APPROVAL', 'COMPLETED', TIMESTAMPADD(DAY, -90, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, 275, CURRENT_TIMESTAMP)),
  (10009, 9, 'GENERAL', 'COMPLETED', TIMESTAMPADD(DAY, -70, CURRENT_TIMESTAMP), NULL),
  (10010, 10, 'GENERAL', 'IN_PROGRESS', TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP), NULL)
ON DUPLICATE KEY UPDATE status = VALUES(status), completed_at = VALUES(completed_at), expires_at = VALUES(expires_at);

-- ---------------------------------------------------------------------------
-- IK-Alkohol data
-- ---------------------------------------------------------------------------

INSERT INTO alcohol_licenses
  (id, organization_id, license_type, license_number, issued_at, expires_at, issuing_authority, notes, created_at, updated_at)
VALUES
  (11001, 1, 'FULL', 'TRD-ALK-2026-771', TIMESTAMPADD(DAY, -220, CURRENT_DATE), TIMESTAMPADD(DAY, 145, CURRENT_DATE), 'Trondheim kommune', 'Gjelder servering inne og ute.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (11002, 1, 'TEMPORARY', 'TRD-ALK-TEMP-021', TIMESTAMPADD(DAY, -30, CURRENT_DATE), TIMESTAMPADD(DAY, 20, CURRENT_DATE), 'Trondheim kommune', 'Midlertidig tillatelse for arrangement.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE expires_at = VALUES(expires_at), notes = VALUES(notes), updated_at = CURRENT_TIMESTAMP;

WITH RECURSIVE age_seq AS (
  SELECT 0 AS n
  UNION ALL
  SELECT n + 1 FROM age_seq WHERE n < 79
)
INSERT INTO age_verification_logs
  (id, organization_id, location_id, verified_by_user_id, verification_method, guest_appeared_underage, id_was_valid, was_refused, note, verified_at, created_at)
SELECT
  12000 + n,
  1,
  1 + MOD(n, 5),
  CASE MOD(n, 5)
    WHEN 0 THEN 2
    WHEN 1 THEN 4
    WHEN 2 THEN 5
    WHEN 3 THEN 8
    ELSE 9
  END,
  CASE MOD(n, 5)
    WHEN 0 THEN 'ID_CHECKED'
    WHEN 1 THEN 'DRIVING_LICENSE_CHECKED'
    WHEN 2 THEN 'PASSPORT_CHECKED'
    WHEN 3 THEN 'KNOWN_REGULAR'
    ELSE 'VISUALLY_OVER_AGE'
  END,
  CASE WHEN MOD(n, 9) = 0 THEN TRUE ELSE FALSE END,
  CASE WHEN MOD(n, 7) = 0 THEN FALSE ELSE TRUE END,
  CASE WHEN MOD(n, 7) = 0 THEN TRUE ELSE FALSE END,
  CASE WHEN MOD(n, 7) = 0 THEN 'Refused due to invalid ID.' ELSE NULL END,
  TIMESTAMPADD(HOUR, -n, CURRENT_TIMESTAMP),
  TIMESTAMPADD(HOUR, -n, CURRENT_TIMESTAMP)
FROM age_seq
ON DUPLICATE KEY UPDATE was_refused = VALUES(was_refused), verified_at = VALUES(verified_at);

WITH RECURSIVE incident_seq AS (
  SELECT 0 AS n
  UNION ALL
  SELECT n + 1 FROM incident_seq WHERE n < 19
)
INSERT INTO alcohol_serving_incidents
  (id, organization_id, location_id, reported_by_user_id, resolved_by_user_id, incident_type, severity, status, description, corrective_action, occurred_at, resolved_at, created_at, updated_at)
SELECT
  13000 + n,
  1,
  1 + MOD(n, 5),
  CASE MOD(n, 4)
    WHEN 0 THEN 2
    WHEN 1 THEN 4
    WHEN 2 THEN 8
    ELSE 9
  END,
  CASE WHEN MOD(n, 3) = 0 THEN 1 ELSE NULL END,
  CASE MOD(n, 6)
    WHEN 0 THEN 'REFUSED_SERVICE'
    WHEN 1 THEN 'INTOXICATED_PERSON'
    WHEN 2 THEN 'UNDERAGE_ATTEMPT'
    WHEN 3 THEN 'OVER_SERVING'
    WHEN 4 THEN 'DISTURBANCE'
    ELSE 'OTHER'
  END,
  CASE MOD(n, 4)
    WHEN 0 THEN 'LOW'
    WHEN 1 THEN 'MEDIUM'
    WHEN 2 THEN 'HIGH'
    ELSE 'CRITICAL'
  END,
  CASE MOD(n, 4)
    WHEN 0 THEN 'OPEN'
    WHEN 1 THEN 'UNDER_REVIEW'
    WHEN 2 THEN 'RESOLVED'
    ELSE 'CLOSED'
  END,
  CONCAT('Auto-seeded incident #', n),
  CASE WHEN MOD(n, 2) = 0 THEN 'Briefed staff and documented incident.' ELSE NULL END,
  TIMESTAMPADD(HOUR, -(n * 6), CURRENT_TIMESTAMP),
  CASE WHEN MOD(n, 4) IN (2, 3) THEN TIMESTAMPADD(HOUR, -(n * 6) + 4, CURRENT_TIMESTAMP) ELSE NULL END,
  TIMESTAMPADD(HOUR, -(n * 6), CURRENT_TIMESTAMP),
  TIMESTAMPADD(HOUR, -(n * 3), CURRENT_TIMESTAMP)
FROM incident_seq
ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = VALUES(updated_at);

-- ---------------------------------------------------------------------------
-- Documents
-- ---------------------------------------------------------------------------

WITH RECURSIVE doc_seq AS (
  SELECT 0 AS n
  UNION ALL
  SELECT n + 1 FROM doc_seq WHERE n < 17
)
INSERT INTO documents
  (id, organization_id, uploaded_by_id, title, description, category, file_name, content_type, file_size, file_data, created_at, updated_at)
SELECT
  14000 + n,
  1,
  CASE MOD(n, 6)
    WHEN 0 THEN 1
    WHEN 1 THEN 2
    WHEN 2 THEN 3
    WHEN 3 THEN 4
    WHEN 4 THEN 8
    ELSE 9
  END,
  CONCAT('Demo dokument ', n + 1),
  CONCAT('Auto-generert testdokument for manuell verifisering #', n + 1),
  CASE MOD(n, 5)
    WHEN 0 THEN 'POLICY'
    WHEN 1 THEN 'TRAINING_MATERIAL'
    WHEN 2 THEN 'CERTIFICATION'
    WHEN 3 THEN 'INSPECTION_REPORT'
    ELSE 'OTHER'
  END,
  CONCAT('demo_document_', n + 1, '.txt'),
  'text/plain',
  LENGTH(CONCAT('seed-file-', n + 1)),
  CAST(CONCAT('seed-file-', n + 1) AS BINARY),
  TIMESTAMPADD(DAY, -n, CURRENT_TIMESTAMP),
  TIMESTAMPADD(DAY, -n, CURRENT_TIMESTAMP)
FROM doc_seq
ON DUPLICATE KEY UPDATE title = VALUES(title), updated_at = VALUES(updated_at);
