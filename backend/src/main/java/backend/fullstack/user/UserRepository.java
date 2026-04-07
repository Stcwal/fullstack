package backend.fullstack.user;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.user.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for User entities.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // List all users within an organization (ADMIN use)
    List<User> findByOrganization_Id(Long organizationId);

    // List users at a specific location
    List<User> findByHomeLocation_Id(Long locationId);

    // Find all supervisors in an org — used when listing who can be assigned to a location
    List<User> findByOrganization_IdAndRole(Long organizationId, Role role);

    long countByOrganization_IdAndRoleAndIsActiveTrue(Long organizationId, Role role);

    @Query("""
        SELECT l.id
        FROM User u
        JOIN u.locations l
        WHERE u.id = :userId
        """)
    List<Long> findAdditionalLocationIdsByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT DISTINCT u FROM User u
    WHERE u.homeLocation.id = :locationId
    OR :locationId IN (SELECT l.id FROM u.locations l)
    """)
    List<User> findAllByLocationId(@Param("locationId") Long locationId);

        @Query("""
            SELECT u.homeLocation.id
            FROM User u
            WHERE u.id = :userId AND u.homeLocation IS NOT NULL
            """)
        List<Long> findHomeLocationIdsByUserId(@Param("userId") Long userId);

        default List<Long> findEffectiveLocationScopeByUserId(Long userId) {
            Set<Long> scope = new LinkedHashSet<>(findHomeLocationIdsByUserId(userId));
            scope.addAll(findAdditionalLocationIdsByUserId(userId));
            return new ArrayList<>(scope);
        }

        @Query("""
                SELECT DISTINCT p.name
                FROM UserProfileAssignment a
                JOIN a.profile p
                WHERE a.user.id = :userId
                    AND p.isActive = true
                    AND (a.startsAt IS NULL OR a.startsAt <= :at)
                    AND (a.endsAt IS NULL OR a.endsAt >= :at)
                """)
        List<String> findActiveProfileNamesByUserId(@Param("userId") Long userId, @Param("at") LocalDateTime at);

        @Query("""
                SELECT DISTINCT o.permission
                FROM UserPermissionOverride o
                WHERE o.user.id = :userId
                    AND o.effect = :effect
                    AND (o.startsAt IS NULL OR o.startsAt <= :at)
                    AND (o.endsAt IS NULL OR o.endsAt >= :at)
                """)
        List<Permission> findActiveOverridePermissionsByUserId(
                        @Param("userId") Long userId,
                        @Param("effect") PermissionEffect effect,
                        @Param("at") LocalDateTime at
        );
}
