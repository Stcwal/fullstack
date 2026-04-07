package backend.fullstack.permission.profile;

import java.time.LocalDateTime;

import backend.fullstack.organization.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a permission profile within an organization. A permission profile is a collection of permissions that can be assigned to users to define their access rights within the system.
 * 
 * Each permission profile belongs to a specific organization and has a unique name within that organization. The profile can be active or inactive, allowing for flexible management of user permissions over time.
 *
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(
        name = "permission_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permission_profile_org_name", columnNames = {"organization_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
