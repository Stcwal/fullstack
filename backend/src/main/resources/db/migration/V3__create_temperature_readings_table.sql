CREATE TABLE temperature_readings (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    unit_id         BIGINT       NOT NULL,
    organization_id BIGINT       NOT NULL,
    recorded_by_id  BIGINT       NOT NULL,
    temperature     DOUBLE       NOT NULL,
    recorded_at     TIMESTAMP    NOT NULL,
    is_out_of_range BOOLEAN      NOT NULL DEFAULT FALSE,
    note            VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_readings_unit         FOREIGN KEY (unit_id)         REFERENCES temperature_units(id),
    CONSTRAINT fk_readings_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_readings_recorded_by  FOREIGN KEY (recorded_by_id)  REFERENCES users(id)
);

CREATE INDEX idx_readings_unit_id         ON temperature_readings(unit_id);
CREATE INDEX idx_readings_organization_id ON temperature_readings(organization_id);
CREATE INDEX idx_readings_recorded_at     ON temperature_readings(recorded_at);
