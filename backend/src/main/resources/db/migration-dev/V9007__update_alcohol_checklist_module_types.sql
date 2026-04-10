-- Template 4105 ('Daglig alderskontroll bar') is IK-Alkohol.
-- All instances spawned from it must carry the same module type.
UPDATE checklist_templates SET module_type = 'IK_ALKOHOL' WHERE id = 4105;
UPDATE checklist_instances  SET module_type = 'IK_ALKOHOL' WHERE template_id = 4105;
