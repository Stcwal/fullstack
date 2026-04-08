CREATE TABLE temperature_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    recorded_by_user_id BIGINT NOT NULL,
    temperature DOUBLE NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    note VARCHAR(500),
    is_deviation BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_temperature_readings_organization_id
    ON temperature_readings(organization_id);

CREATE INDEX idx_temperature_readings_unit_id
    ON temperature_readings(unit_id);

CREATE INDEX idx_temperature_readings_recorded_at
    ON temperature_readings(recorded_at);

CREATE INDEX idx_temperature_readings_is_deviation
    ON temperature_readings(is_deviation);
