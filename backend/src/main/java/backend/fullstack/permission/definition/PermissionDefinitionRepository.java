package backend.fullstack.permission.definition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing PermissionDefinition entities.
 * Provides methods to perform CRUD operations and custom queries related to permission definitions within the system.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface PermissionDefinitionRepository extends JpaRepository<PermissionDefinition, Long> {
    Optional<PermissionDefinition> findByPermissionKey(String permissionKey);
}
