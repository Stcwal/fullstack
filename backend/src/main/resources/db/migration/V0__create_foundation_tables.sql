-- ============================================================
-- Foundation tables: organizations, locations, users, permissions
-- Must run before V1 (checklists), which already references organization_id
-- ============================================================

CREATE TABLE IF NOT EXISTS organizations (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    org_number  VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT organizations_org_number UNIQUE (org_number)
);

CREATE TABLE IF NOT EXISTS locations (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_locations_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE INDEX idx_locations_organization_id ON locations(organization_id);

CREATE TABLE IF NOT EXISTS users (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT,
    organization_id  BIGINT       NOT NULL,
    home_location_id BIGINT,
    email            VARCHAR(255) NOT NULL UNIQUE,
    first_name       VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    role             VARCHAR(30)  NOT NULL,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_organization  FOREIGN KEY (organization_id)  REFERENCES organizations(id),
    CONSTRAINT fk_users_home_location FOREIGN KEY (home_location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS user_locations (
    user_id     BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, location_id),
    CONSTRAINT fk_user_locations_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_user_locations_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE IF NOT EXISTS user_location_scope_assignments (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    location_id     BIGINT       NOT NULL,
    starts_at       TIMESTAMP,
    ends_at         TIMESTAMP,
    assignment_mode VARCHAR(20)  NOT NULL DEFAULT 'INHERIT',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    completed_at    TIMESTAMP,
    confirmed_at    TIMESTAMP,
    reason          VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ulsa_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_ulsa_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- ============================================================
-- Permission system
-- ============================================================

CREATE TABLE IF NOT EXISTS permissions (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    permission_key VARCHAR(120) NOT NULL UNIQUE,
    description    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    id            BIGINT      PRIMARY KEY AUTO_INCREMENT,
    role          VARCHAR(30) NOT NULL,
    permission_id BIGINT      NOT NULL,
    CONSTRAINT uk_role_permission       UNIQUE (role, permission_id),
    CONSTRAINT fk_role_permissions_perm FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS user_permission_overrides (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    permission_key VARCHAR(80)  NOT NULL,
    effect         VARCHAR(10)  NOT NULL,
    scope          VARCHAR(20)  NOT NULL,
    location_id    BIGINT,
    starts_at      TIMESTAMP,
    ends_at        TIMESTAMP,
    reason         VARCHAR(255),
    CONSTRAINT fk_user_perm_overrides_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS permission_profiles (
    id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT       NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(255),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perm_profiles_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT uk_perm_profiles_org_name UNIQUE (organization_id, name)
);

CREATE TABLE IF NOT EXISTS permission_profile_bindings (
    id             BIGINT      PRIMARY KEY AUTO_INCREMENT,
    profile_id     BIGINT      NOT NULL,
    permission_key VARCHAR(80) NOT NULL,
    scope          VARCHAR(20) NOT NULL,
    location_id    BIGINT,
    condition_type VARCHAR(30) NOT NULL,
    CONSTRAINT fk_perm_profile_bindings_profile FOREIGN KEY (profile_id) REFERENCES permission_profiles(id),
    CONSTRAINT uk_perm_profile_binding UNIQUE (profile_id, permission_key)
);

CREATE TABLE IF NOT EXISTS user_profile_assignments (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    profile_id  BIGINT    NOT NULL,
    location_id BIGINT,
    starts_at   TIMESTAMP,
    ends_at     TIMESTAMP,
    CONSTRAINT fk_user_profile_assign_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_user_profile_assign_profile FOREIGN KEY (profile_id) REFERENCES permission_profiles(id),
    CONSTRAINT fk_user_profile_assign_loc     FOREIGN KEY (location_id) REFERENCES locations(id),
    CONSTRAINT uk_user_profile_assignment     UNIQUE (user_id, profile_id)
);
