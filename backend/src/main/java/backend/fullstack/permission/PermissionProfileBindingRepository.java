package backend.fullstack.permission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing PermissionProfileBinding entities.
 * Provides methods to perform CRUD operations and custom queries related to permission profile bindings.
 *
 * @version 1.0
 * @since 31.03.26
 */
public interface PermissionProfileBindingRepository extends JpaRepository<PermissionProfileBinding, Long> {
    List<PermissionProfileBinding> findByProfile_IdIn(List<Long> profileIds);
}
