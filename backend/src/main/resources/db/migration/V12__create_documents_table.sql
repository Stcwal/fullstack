CREATE TABLE IF NOT EXISTS documents (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    organization_id BIGINT        NOT NULL,
    uploaded_by_id  BIGINT        NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    description     VARCHAR(1000) NULL,
    category        VARCHAR(50)   NOT NULL,
    file_name       VARCHAR(255)  NOT NULL,
    content_type    VARCHAR(100)  NOT NULL,
    file_size       BIGINT        NOT NULL,
    file_data       LONGBLOB      NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_documents_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_documents_uploaded_by
        FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);

CREATE INDEX idx_documents_organization_id ON documents(organization_id);
CREATE INDEX idx_documents_category ON documents(category);
CREATE INDEX idx_documents_uploaded_by_id ON documents(uploaded_by_id);
