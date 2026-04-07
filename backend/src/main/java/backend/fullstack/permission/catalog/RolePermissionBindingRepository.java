package backend.fullstack.permission.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.fullstack.user.role.Role;

/**
 * Repository for managing role-permission bindings.
 * 
 * @version 1.0
 * @since 31.03.26
 */
public interface RolePermissionBindingRepository extends JpaRepository<RolePermissionBinding, Long> {
    List<RolePermissionBinding> findByRole(Role role);
    boolean existsByRole(Role role);
}
