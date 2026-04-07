CREATE TABLE temperature_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    target_temperature DOUBLE NOT NULL,
    min_threshold DOUBLE NOT NULL,
    max_threshold DOUBLE NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_temperature_units_organization_id
    ON temperature_units(organization_id);

CREATE INDEX idx_temperature_units_active
    ON temperature_units(active);