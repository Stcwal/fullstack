package backend.fullstack.permission;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing UserProfileAssignment entities.
 * Provides methods to perform CRUD operations and custom queries related to user profile assignments.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface UserProfileAssignmentRepository extends JpaRepository<UserProfileAssignment, Long> {

    @Query("""
        SELECT a
        FROM UserProfileAssignment a
        WHERE a.user.id = :userId
          AND (a.startsAt IS NULL OR a.startsAt <= :at)
          AND (a.endsAt IS NULL OR a.endsAt >= :at)
        """)
    List<UserProfileAssignment> findActiveByUserId(@Param("userId") Long userId, @Param("at") LocalDateTime at);

    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId AND a.location.id = :locationId")
    void deleteAllByUserIdAndLocationId(@Param("userId") Long userId, @Param("locationId") Long locationId);

    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId AND a.location IS NULL")
    void deleteAllGlobalByUserId(@Param("userId") Long userId);
}
