package backend.fullstack.permission.override;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import java.time.LocalDateTime;

import backend.fullstack.user.User;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user-specific permission override, allowing for fine-grained access control beyond role-based permissions. This entity captures both granted and denied permissions for a user, with optional scoping to specific locations and time periods.
 * 
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(name = "user_permission_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_key", nullable = false, length = 80)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false, length = 10)
    private PermissionEffect effect;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    @Builder.Default
    private PermissionScope scope = PermissionScope.ORGANIZATION;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "reason", length = 255)
    private String reason;
}
