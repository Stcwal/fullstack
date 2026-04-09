package backend.fullstack.permission.profile;

import java.time.LocalDateTime;

import backend.fullstack.location.Location;
import backend.fullstack.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entity representing an assignment of a permission profile to a user.
 *
 * <p>An assignment links a user to one profile, either organization-wide (no location)
 * or location-scoped (specific location). The optional start and end timestamps define
 * when the assignment is active.</p>
 *
 * <p>Lombok generates standard accessors/mutators, a no-args constructor,
 * an all-args constructor, and a builder for this entity.</p>
 *
 * @version 1.0
 * @since 31.03.26
 */
@Entity
@Table(
        name = "user_profile_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profile_assignment", columnNames = {"user_id", "profile_id", "location_id", "starts_at"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileAssignment {

    /**
     * Surrogate primary key for this assignment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User receiving the permission profile assignment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Permission profile being assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private PermissionProfile profile;

    /**
     * Optional location for location-scoped assignments.
     * If null, the assignment is treated as organization-scoped.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    /**
     * Optional timestamp from when the assignment becomes active.
     * If null, the assignment is active immediately.
     */
    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    /**
     * Optional timestamp when the assignment expires.
     * If null, the assignment remains active until explicitly ended.
     */
    @Column(name = "ends_at")
    private LocalDateTime endsAt;
}
