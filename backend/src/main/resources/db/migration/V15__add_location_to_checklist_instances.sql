ALTER TABLE checklist_instances
    ADD COLUMN location_id BIGINT NULL;

ALTER TABLE checklist_instances
    ADD CONSTRAINT fk_checklist_instances_location
        FOREIGN KEY (location_id) REFERENCES locations(id);

CREATE INDEX idx_checklist_instances_location_id ON checklist_instances(location_id);
