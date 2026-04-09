package backend.fullstack.permission.profile;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionConditionType;
import backend.fullstack.permission.model.PermissionScope;
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
 * Entity representing the binding of a permission to a permission profile. This defines which permissions are included in each profile, along with the scope and any conditions that apply to the permission.
 * 
 * Each instance of PermissionProfileBinding represents a single permission assignment to a specific profile. The combination of profile, permission, scope, location, and condition type is unique, ensuring that a profile cannot have duplicate bindings for the same permission and scope.
 *
 * <p>The entity is typically created via Lombok's generated builder. When values are not provided,
 * {@code scope} defaults to {@link PermissionScope#ORGANIZATION} and {@code conditionType} defaults
 * to {@link PermissionConditionType#NONE}.</p>
 * 
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(
        name = "permission_profile_bindings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_profile_permission_scope",
                        columnNames = {"profile_id", "permission_key", "scope", "location_id", "condition_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionProfileBinding {

        /**
         * Surrogate primary key for this binding.
         */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        /**
         * The permission profile that owns this binding.
         */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private PermissionProfile profile;

        /**
         * The permission key granted by this binding.
         */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_key", nullable = false, length = 80)
    private Permission permission;

        /**
         * Scope for where the permission applies.
         * Defaults to {@link PermissionScope#ORGANIZATION} when omitted in the builder.
         */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    @Builder.Default
    private PermissionScope scope = PermissionScope.ORGANIZATION;

        /**
         * Optional location identifier used when {@link #scope} is location-scoped.
         */
    @Column(name = "location_id")
    private Long locationId;

        /**
         * Optional runtime condition attached to this binding.
         * Defaults to {@link PermissionConditionType#NONE} when omitted in the builder.
         */
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    @Builder.Default
    private PermissionConditionType conditionType = PermissionConditionType.NONE;
}
