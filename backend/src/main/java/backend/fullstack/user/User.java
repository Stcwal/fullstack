package backend.fullstack.user;

import backend.fullstack.location.Location;
import backend.fullstack.organization.Organization;
import backend.fullstack.user.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Represents a user in the IK-Control system. Each user belongs to one organization and can have different roles and permissions.
 *
 * @version 1.0
 * @since 26.03.26
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /**
     * Primary key for the User entity. This is an auto-generated unique identifier for each user in the system.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The organization this user belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Home location for MANAGER and STAFF users.
     * ADMIN and SUPERVISOR users typically have this as null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_location_id")
    private Location homeLocation;

    /**
     * Additional locations where a user can temporarily/permanently work.
     * This supports "rented" users without introducing a complex assignment model.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_locations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<Location> locations = new HashSet<>();

    /**
     * Unique email address used for authentication and communication.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * First name of the user.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Last name of the user.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Hashed password for authentication.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * User role, which determines permissions and access levels within the application.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Indicates whether the user account is active.
     */
    @Column(name = "is_active")
    private boolean isActive = true;

    /**
     * Timestamp of when the user was created.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the creation timestamp before persisting a new user.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    /**
     * Returns the authorities granted to the user based on their role.
     * Each role corresponds to a single authority in the format "ROLE_{ROLE_NAME}".
     * For example, a user with role MANAGER will have the authority "ROLE_MANAGER".
     * This method is used by Spring Security to perform authorization checks.
     *
     * @return a collection of granted authorities for the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the password hash used for authentication. This is the hashed version of the user's password,
     * not the plain text password. The authentication provider will compare this hash against the hash of the
     * password provided during login. It is crucial that this field is properly hashed and never exposed in plain text.
     *
     * @return the password hash for authentication purposes
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Returns the username used for authentication. In thi application we use email as the unique identifier for users.
     *
     * @return the email address of the user, which serves as the username for authentication
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired. An expired account cannot be authenticated.
     *
     * @return true if the user's account is valid (i.e., non-expired), false if no longer valid (i.e., expired)
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * Indicates whether the user's account is locked. A locked account cannot be authenticated.
     *
     * @return true if the user's account is not locked, false if it is locked
     */
    @Override
    public boolean isAccountNonLocked() { return isActive; }

    /**
     * Indicates whether the user's credentials (password) has expired. Expired credentials prevent authentication.
     *
     * @return true if the user's credentials are valid (i.e., non-expired), false if expired
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated.
     *
     * @return true if the user is enabled (active), false if disabled (inactive)
     */
    @Override
    public boolean isEnabled() { return isActive; }

    /**
     * Convenience method to get the organization ID directly from the user entity.
     *
     * @return the ID of the organization this user belongs to, or null if the organization is not set
     */
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    /**
     * Convenience method to get the home location ID directly from the user entity.
     *
     * @return the ID of the home location this user is associated with, or null if not set
     */
    public Long getHomeLocationId() {
        return homeLocation != null ? homeLocation.getId() : null;
    }
}