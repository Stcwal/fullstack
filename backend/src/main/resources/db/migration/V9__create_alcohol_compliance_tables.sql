-- =============================================================
-- V9: IK-Alkohol – Alcohol compliance tables
-- =============================================================

-- Organization alcohol license / bevilling
CREATE TABLE IF NOT EXISTS alcohol_licenses (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    license_type    VARCHAR(30)  NOT NULL,
    license_number  VARCHAR(100) NULL,
    issued_at       DATE         NULL,
    expires_at      DATE         NULL,
    issuing_authority VARCHAR(255) NULL,
    notes           TEXT         NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_alcohol_licenses_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
CREATE INDEX idx_alcohol_licenses_org ON alcohol_licenses(organization_id);

-- Age verification logs
CREATE TABLE IF NOT EXISTS age_verification_logs (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id     BIGINT       NOT NULL,
    location_id         BIGINT       NOT NULL,
    verified_by_user_id BIGINT       NOT NULL,
    verification_method VARCHAR(30)  NOT NULL,
    guest_appeared_underage BOOLEAN  NOT NULL DEFAULT TRUE,
    id_was_valid        BOOLEAN      NULL,
    was_refused         BOOLEAN      NOT NULL DEFAULT FALSE,
    note                TEXT         NULL,
    verified_at         TIMESTAMP    NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_age_verification_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_age_verification_location     FOREIGN KEY (location_id)     REFERENCES locations(id),
    CONSTRAINT fk_age_verification_user         FOREIGN KEY (verified_by_user_id) REFERENCES users(id)
);
CREATE INDEX idx_age_verification_org      ON age_verification_logs(organization_id);
CREATE INDEX idx_age_verification_location ON age_verification_logs(location_id);
CREATE INDEX idx_age_verification_date     ON age_verification_logs(verified_at);

-- Alcohol serving incidents
CREATE TABLE IF NOT EXISTS alcohol_serving_incidents (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    organization_id     BIGINT       NOT NULL,
    location_id         BIGINT       NOT NULL,
    reported_by_user_id BIGINT       NOT NULL,
    resolved_by_user_id BIGINT       NULL,
    incident_type       VARCHAR(40)  NOT NULL,
    severity            VARCHAR(20)  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    description         TEXT         NOT NULL,
    corrective_action   TEXT         NULL,
    occurred_at         TIMESTAMP    NOT NULL,
    resolved_at         TIMESTAMP    NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_alcohol_incident_organization FOREIGN KEY (organization_id)     REFERENCES organizations(id),
    CONSTRAINT fk_alcohol_incident_location     FOREIGN KEY (location_id)         REFERENCES locations(id),
    CONSTRAINT fk_alcohol_incident_reporter     FOREIGN KEY (reported_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_alcohol_incident_resolver     FOREIGN KEY (resolved_by_user_id) REFERENCES users(id)
);
CREATE INDEX idx_alcohol_incident_org      ON alcohol_serving_incidents(organization_id);
CREATE INDEX idx_alcohol_incident_location ON alcohol_serving_incidents(location_id);
CREATE INDEX idx_alcohol_incident_status   ON alcohol_serving_incidents(status);
CREATE INDEX idx_alcohol_incident_date     ON alcohol_serving_incidents(occurred_at);
