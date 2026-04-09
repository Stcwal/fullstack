package backend.fullstack.auth.invite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for one-time invite tokens.
 */
public interface UserInviteTokenRepository extends JpaRepository<UserInviteToken, Long> {

    /**
     * Finds a token row by its SHA-256 token hash.
     */
    Optional<UserInviteToken> findByTokenHash(String tokenHash);

    /**
     * Deletes still-open invite tokens for a user before issuing a new one.
     */
    void deleteByUserIdAndConsumedAtIsNull(Long userId);
}
