-- Assign existing checklist instances/templates to locations.
-- Without this, instances seeded before V15 have location_id = NULL
-- and are invisible when a location filter is applied.

-- Existing instances → location 1 (Innherredsveien 1, main kitchen)
UPDATE checklist_instances SET location_id = 1 WHERE id IN (4301,4302,4303,4304,4305,4306,4307,4308,4309,4310);

-- ---------------------------------------------------------------------------
-- Location 2 (Munkegata 14) — daily opening + weekly freezer
-- ---------------------------------------------------------------------------

INSERT INTO checklist_instances (id, template_id, organization_id, location_id, title, frequency, date, status)
VALUES
  (4401, 4101, 1, 2, 'Daglig åpning kjøkken', 'DAILY', CURRENT_DATE, 'PENDING'),
  (4402, 4102, 1, 2, 'Daglig stenging kjøkken', 'DAILY', CURRENT_DATE, 'PENDING'),
  (4403, 4103, 1, 2, 'Ukentlig renhold fryserom', 'WEEKLY', CURRENT_DATE, 'IN_PROGRESS')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4501, 4401, 'Desinfiser alle arbeidsflater', FALSE, NULL, NULL),
  (4502, 4401, 'Kalibrer termometer', FALSE, NULL, NULL),
  (4503, 4401, 'Kontroller leveringstemperatur for fisk', FALSE, NULL, NULL),
  (4504, 4402, 'Logg slutt-temperatur på alle enheter', FALSE, NULL, NULL),
  (4505, 4402, 'Lukk avvik i systemet', FALSE, NULL, NULL),
  (4506, 4403, 'Tin og rengjor fordamper', TRUE, 4, TIMESTAMPADD(HOUR, -4, CURRENT_TIMESTAMP)),
  (4507, 4403, 'Kontroller pakninger og dorlister', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);

-- ---------------------------------------------------------------------------
-- Location 3 (Nedre Elvehavn 5) — daily opening + alcohol control
-- ---------------------------------------------------------------------------

INSERT INTO checklist_instances (id, template_id, organization_id, location_id, title, frequency, date, status)
VALUES
  (4411, 4101, 1, 3, 'Daglig åpning kjøkken', 'DAILY', CURRENT_DATE, 'COMPLETED'),
  (4412, 4105, 1, 3, 'Daglig alderskontroll bar', 'DAILY', CURRENT_DATE, 'PENDING')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4511, 4411, 'Desinfiser alle arbeidsflater', TRUE, 6, TIMESTAMPADD(HOUR, -5, CURRENT_TIMESTAMP)),
  (4512, 4411, 'Kalibrer termometer', TRUE, 6, TIMESTAMPADD(HOUR, -4, CURRENT_TIMESTAMP)),
  (4513, 4411, 'Kontroller leveringstemperatur for fisk', TRUE, 6, TIMESTAMPADD(HOUR, -3, CURRENT_TIMESTAMP)),
  (4514, 4412, 'Brief ansatte på legitimasjonsrutiner', FALSE, NULL, NULL),
  (4515, 4412, 'Test avviksflyt for underaarige', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);

-- ---------------------------------------------------------------------------
-- Location 4 (Solsiden Brygge 3) — daily opening
-- ---------------------------------------------------------------------------

INSERT INTO checklist_instances (id, template_id, organization_id, location_id, title, frequency, date, status)
VALUES
  (4421, 4101, 1, 4, 'Daglig åpning kjøkken', 'DAILY', CURRENT_DATE, 'IN_PROGRESS'),
  (4422, 4102, 1, 4, 'Daglig stenging kjøkken', 'DAILY', CURRENT_DATE, 'PENDING')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4521, 4421, 'Desinfiser alle arbeidsflater', TRUE, 9, TIMESTAMPADD(HOUR, -2, CURRENT_TIMESTAMP)),
  (4522, 4421, 'Kalibrer termometer', FALSE, NULL, NULL),
  (4523, 4421, 'Kontroller leveringstemperatur for fisk', FALSE, NULL, NULL),
  (4524, 4422, 'Logg slutt-temperatur på alle enheter', FALSE, NULL, NULL),
  (4525, 4422, 'Lukk avvik i systemet', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);
