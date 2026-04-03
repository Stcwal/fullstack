package backend.fullstack.permission.catalog;

import backend.fullstack.permission.definition.PermissionDefinition;
import backend.fullstack.user.role.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the binding between a user role and a permission. This defines which permissions are granted to each role in the system.
 * 
 * Each instance of RolePermissionBinding represents a single permission granted to a specific role. The combination of role and permission is unique, ensuring that a role cannot have duplicate permissions.
 *
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_permission", columnNames = {"role", "permission_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private PermissionDefinition permission;
}
