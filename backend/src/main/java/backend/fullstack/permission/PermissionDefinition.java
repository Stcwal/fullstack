package backend.fullstack.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a permission definition in the IK-Control system. Each permission definition has a unique key and an optional description that explains the purpose of the permission.
 * 
 * Permission definitions are used as the basis for assigning permissions to roles, profiles, and users. They define the specific actions or access rights that can be granted within the system.
 *
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_key", nullable = false, unique = true, length = 120)
    private String permissionKey;

    @Column(name = "description", length = 255)
    private String description;
}