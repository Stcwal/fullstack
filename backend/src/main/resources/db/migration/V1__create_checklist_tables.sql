CREATE TABLE checklist_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    frequency VARCHAR(20) NOT NULL
);

CREATE TABLE checklist_template_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    item_text VARCHAR(250) NOT NULL,
    CONSTRAINT fk_checklist_template_item_template
        FOREIGN KEY (template_id) REFERENCES checklist_templates(id)
        ON DELETE CASCADE
);

CREATE TABLE checklist_instances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_checklist_instance_template
        FOREIGN KEY (template_id) REFERENCES checklist_templates(id)
        ON DELETE CASCADE
);

CREATE TABLE checklist_instance_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    item_text VARCHAR(250) NOT NULL,
    completed BOOLEAN NOT NULL,
    completed_by_user_id BIGINT,
    completed_at TIMESTAMP,
    CONSTRAINT fk_checklist_instance_item_instance
        FOREIGN KEY (instance_id) REFERENCES checklist_instances(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_checklist_templates_org
    ON checklist_templates (organization_id);

CREATE INDEX idx_checklist_instances_org
    ON checklist_instances (organization_id);

CREATE INDEX idx_checklist_instances_template
    ON checklist_instances (template_id);

CREATE INDEX idx_checklist_instance_items_instance
    ON checklist_instance_items (instance_id);
