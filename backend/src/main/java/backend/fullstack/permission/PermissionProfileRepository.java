package backend.fullstack.permission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing PermissionProfile entities.
 * Provides methods to perform CRUD operations and custom queries related to permission profiles within an organization.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface PermissionProfileRepository extends JpaRepository<PermissionProfile, Long> {
    List<PermissionProfile> findByOrganization_IdAndIsActiveTrue(Long organizationId);
    List<PermissionProfile> findByIdInAndOrganization_IdAndIsActiveTrue(List<Long> ids, Long organizationId);
    Optional<PermissionProfile> findByIdAndOrganization_Id(Long id, Long organizationId);
}
