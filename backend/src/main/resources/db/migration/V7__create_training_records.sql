CREATE TABLE IF NOT EXISTS training_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    training_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    completed_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    CONSTRAINT fk_training_records_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_training_records_user_id ON training_records(user_id);
CREATE INDEX idx_training_records_training_type ON training_records(training_type);
CREATE INDEX idx_training_records_status ON training_records(status);
