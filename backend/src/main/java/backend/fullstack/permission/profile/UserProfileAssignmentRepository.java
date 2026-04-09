package backend.fullstack.permission.profile;

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
 * <p>Custom queries in this repository are used to resolve active assignments at a point in time
 * and to remove assignment groups by user and scope.</p>
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface UserProfileAssignmentRepository extends JpaRepository<UserProfileAssignment, Long> {

  /**
   * Finds assignments for a user that are active at a specific timestamp.
   *
   * <p>An assignment is considered active when:</p>
   * <ul>
   *   <li>{@code startsAt} is null or less than/equal to {@code at}</li>
   *   <li>{@code endsAt} is null or greater than/equal to {@code at}</li>
   * </ul>
   *
   * @param userId the user identifier
   * @param at the instant used to evaluate assignment activity
   * @return active assignments for the user at the provided timestamp
   */
    @Query("""
        SELECT a
        FROM UserProfileAssignment a
        WHERE a.user.id = :userId
          AND (a.startsAt IS NULL OR a.startsAt <= :at)
          AND (a.endsAt IS NULL OR a.endsAt >= :at)
        """)
    List<UserProfileAssignment> findActiveByUserId(@Param("userId") Long userId, @Param("at") LocalDateTime at);

    /**
     * Deletes all profile assignments for the specified user.
     *
     * @param userId the user identifier
     */
    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Deletes all profile assignments for the specified user at a specific location.
     *
     * @param userId the user identifier
     * @param locationId the location identifier
     */
    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId AND a.location.id = :locationId")
    void deleteAllByUserIdAndLocationId(@Param("userId") Long userId, @Param("locationId") Long locationId);

    /**
     * Deletes all organization-scoped (global) assignments for the specified user.
     *
     * <p>Global assignments are those where {@code location} is null.</p>
     *
     * @param userId the user identifier
     */
    @Modifying
    @Query("DELETE FROM UserProfileAssignment a WHERE a.user.id = :userId AND a.location IS NULL")
    void deleteAllGlobalByUserId(@Param("userId") Long userId);
}
