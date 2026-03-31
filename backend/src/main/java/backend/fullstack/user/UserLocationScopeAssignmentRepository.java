package backend.fullstack.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing UserLocationScopeAssignment entities.
 * Provides methods to perform CRUD operations and custom queries related to user location scope assignments.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface UserLocationScopeAssignmentRepository extends JpaRepository<UserLocationScopeAssignment, Long> {

    @Query("""
        SELECT a.location.id
        FROM UserLocationScopeAssignment a
        WHERE a.user.id = :userId
          AND a.status IN (
                backend.fullstack.user.TemporaryAssignmentStatus.ACTIVE,
                backend.fullstack.user.TemporaryAssignmentStatus.SCHEDULED
          )
          AND (a.startsAt IS NULL OR a.startsAt <= :at)
          AND (a.endsAt IS NULL OR a.endsAt >= :at)
        """)
    List<Long> findActiveLocationIdsByUserId(@Param("userId") Long userId, @Param("at") LocalDateTime at);

    @Query("""
        SELECT COUNT(a) > 0
        FROM UserLocationScopeAssignment a
        WHERE a.user.id = :userId
          AND a.location.id = :locationId
          AND a.status IN (
                backend.fullstack.user.TemporaryAssignmentStatus.ACTIVE,
                backend.fullstack.user.TemporaryAssignmentStatus.SCHEDULED
          )
          AND a.mode = backend.fullstack.user.TemporaryAssignmentMode.ACTING
          AND (a.startsAt IS NULL OR a.startsAt <= :at)
          AND (a.endsAt IS NULL OR a.endsAt >= :at)
        """)
    boolean existsActiveActingAssignment(
            @Param("userId") Long userId,
            @Param("locationId") Long locationId,
            @Param("at") LocalDateTime at
    );
}
