CREATE TABLE IF NOT EXISTS user_invite_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uk_user_invite_tokens_token_hash ON user_invite_tokens(token_hash);
CREATE INDEX idx_user_invite_tokens_user_id ON user_invite_tokens(user_id);
