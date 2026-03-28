package backend.fullstack.user;

import backend.fullstack.user.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

    // List all users within an organization (ADMIN use)
    List<User> findByOrganization_Id(Long organizationId);

    // List users at a specific location
    List<User> findByHomeLocation_Id(Long locationId);

    // Find all supervisors in an org — used when listing who can be assigned to a location
    List<User> findByOrganization_IdAndRole(Long organizationId, Role role);

    @Query("""
        SELECT l.id
        FROM User u
        JOIN u.locations l
        WHERE u.id = :userId
        """)
    List<Long> findAdditionalLocationIdsByUserId(@Param("userId") Long userId);
}
