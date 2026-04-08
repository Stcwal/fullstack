package backend.fullstack.auth.invite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInviteTokenRepository extends JpaRepository<UserInviteToken, Long> {
    Optional<UserInviteToken> findByTokenHash(String tokenHash);
    void deleteByUserIdAndConsumedAtIsNull(Long userId);
}
