INSERT INTO checklist_templates (id, organization_id, title, frequency)
VALUES
    (1001, 1, 'Åpning kjøkken', 'DAILY'),
    (1002, 1, 'Ukentlig stenging', 'WEEKLY'),
    (2001, 2, 'Daglig bar-sjekk', 'DAILY');

INSERT INTO checklist_template_items (template_id, item_text)
VALUES
    (1001, 'Vask og desinfiser arbeidsflater'),
    (1001, 'Kontroller frysetemperatur'),
    (1001, 'Bytt hansker ved oppstart'),
    (1002, 'Sikre lagerrom'),
    (1002, 'Slukk alt ikke-kritisk utstyr'),
    (2001, 'Kontroller tappelinjer'),
    (2001, 'Sjekk alkoholbeholdning');

INSERT INTO checklist_instances (id, template_id, organization_id, title, frequency, date, status)
VALUES
    (3001, 1001, 1, 'Åpning kjøkken', 'DAILY', CURRENT_DATE, 'IN_PROGRESS'),
    (3002, 1002, 1, 'Ukentlig stenging', 'WEEKLY', CURRENT_DATE, 'PENDING'),
    (3003, 2001, 2, 'Daglig bar-sjekk', 'DAILY', CURRENT_DATE, 'COMPLETED');

INSERT INTO checklist_instance_items (instance_id, item_text, completed, completed_by_user_id, completed_at)
VALUES
    (3001, 'Vask og desinfiser arbeidsflater', TRUE, 1, CURRENT_TIMESTAMP),
    (3001, 'Kontroller frysetemperatur', FALSE, NULL, NULL),
    (3001, 'Bytt hansker ved oppstart', FALSE, NULL, NULL),
    (3002, 'Sikre lagerrom', FALSE, NULL, NULL),
    (3002, 'Slukk alt ikke-kritisk utstyr', FALSE, NULL, NULL),
    (3003, 'Kontroller tappelinjer', TRUE, 2, CURRENT_TIMESTAMP),
    (3003, 'Sjekk alkoholbeholdning', TRUE, 2, CURRENT_TIMESTAMP);
