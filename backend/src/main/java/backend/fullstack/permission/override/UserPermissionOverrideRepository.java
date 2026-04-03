package backend.fullstack.permission.override;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing UserPermissionOverride entities.
 * Provides methods to perform CRUD operations and custom queries related to user permission overrides.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {

    @Query("""
        SELECT o
        FROM UserPermissionOverride o
        WHERE o.user.id = :userId
          AND (o.startsAt IS NULL OR o.startsAt <= :at)
          AND (o.endsAt IS NULL OR o.endsAt >= :at)
        """)
    List<UserPermissionOverride> findActiveByUserId(@Param("userId") Long userId, @Param("at") LocalDateTime at);
}
