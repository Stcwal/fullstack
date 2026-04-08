CREATE TABLE deviations (
    id                   BIGINT       PRIMARY KEY AUTO_INCREMENT,
    organization_id      BIGINT       NOT NULL,
    title                VARCHAR(200) NOT NULL,
    description          VARCHAR(2000),
    status               VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    severity             VARCHAR(30)  NOT NULL,
    module_type          VARCHAR(30)  NOT NULL,
    reported_by_id       BIGINT       NOT NULL,
    resolved_by_id       BIGINT,
    resolved_at          TIMESTAMP,
    resolution           VARCHAR(2000),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_deviations_organization  FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_deviations_reported_by   FOREIGN KEY (reported_by_id)  REFERENCES users(id),
    CONSTRAINT fk_deviations_resolved_by   FOREIGN KEY (resolved_by_id)  REFERENCES users(id)
);

CREATE INDEX idx_deviations_organization_id ON deviations(organization_id);
CREATE INDEX idx_deviations_status          ON deviations(status);
CREATE INDEX idx_deviations_created_at      ON deviations(created_at);
