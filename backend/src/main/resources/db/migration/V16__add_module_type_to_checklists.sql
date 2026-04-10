ALTER TABLE checklist_templates
    ADD COLUMN module_type VARCHAR(20) NOT NULL DEFAULT 'IK_MAT';

ALTER TABLE checklist_instances
    ADD COLUMN module_type VARCHAR(20) NOT NULL DEFAULT 'IK_MAT';
