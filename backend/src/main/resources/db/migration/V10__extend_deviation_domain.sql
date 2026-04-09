ALTER TABLE deviations
    ADD COLUMN related_reading_id BIGINT NULL,
    ADD CONSTRAINT fk_deviations_related_reading
        FOREIGN KEY (related_reading_id) REFERENCES temperature_readings(id);

CREATE INDEX idx_deviations_severity ON deviations(severity);
CREATE INDEX idx_deviations_module_type ON deviations(module_type);
CREATE INDEX idx_deviations_related_reading_id ON deviations(related_reading_id);

CREATE TABLE deviation_comments (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    deviation_id    BIGINT       NOT NULL,
    created_by_id   BIGINT       NOT NULL,
    comment_text    VARCHAR(2000) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_deviation_comments_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_deviation_comments_deviation
        FOREIGN KEY (deviation_id) REFERENCES deviations(id) ON DELETE CASCADE,
    CONSTRAINT fk_deviation_comments_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE INDEX idx_deviation_comments_organization_id ON deviation_comments(organization_id);
CREATE INDEX idx_deviation_comments_deviation_id ON deviation_comments(deviation_id);
CREATE INDEX idx_deviation_comments_created_by_id ON deviation_comments(created_by_id);
CREATE INDEX idx_deviation_comments_created_at ON deviation_comments(created_at);