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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private PermissionProfile profile;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "location_id")
        private Location location;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;
}
