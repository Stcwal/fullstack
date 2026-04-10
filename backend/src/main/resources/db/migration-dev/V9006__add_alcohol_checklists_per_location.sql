-- Add alcohol control checklist instances for locations that were missing them.
-- Template 4105 = 'Daglig alderskontroll bar' (DAILY).
-- Location 1 already has instance 4304; location 3 has 4412 from V9005.

-- Location 2 (Munkegata 14)
INSERT INTO checklist_instances (id, template_id, organization_id, location_id, title, frequency, date, status)
VALUES (4430, 4105, 1, 2, 'Daglig alderskontroll bar', 'DAILY', CURRENT_DATE, 'PENDING')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4531, 4430, 'Brief ansatte på legitimasjonsrutiner', FALSE, NULL, NULL),
  (4532, 4430, 'Test avviksflyt for underaarige', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);

-- Location 4 (Solsiden Brygge 3)
INSERT INTO checklist_instances (id, template_id, organization_id, location_id, title, frequency, date, status)
VALUES (4431, 4105, 1, 4, 'Daglig alderskontroll bar', 'DAILY', CURRENT_DATE, 'IN_PROGRESS')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO checklist_instance_items (id, instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
  (4533, 4431, 'Brief ansatte på legitimasjonsrutiner', TRUE, 9, TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP)),
  (4534, 4431, 'Test avviksflyt for underaarige', FALSE, NULL, NULL)
ON DUPLICATE KEY UPDATE completed = VALUES(completed);
