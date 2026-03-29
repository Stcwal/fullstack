package backend.fullstack.organization;

import backend.fullstack.location.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tenant organization in IK-Control.
 * Each organization is an isolated business domain.
 *
 * @version 1.0
 * @since 26.03.26
 */
@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(name = "organizations_org_number", columnNames = "org_number")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Organization {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Organization display name.
     */
    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must be at most 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Norwegian organization number (9 digits), stored as text.
     */
    @NotBlank(message = "Organization number is required")
    @Pattern(regexp = "\\d{9}", message = "Organization number must be exactly 9 digits")
    @Column(name = "org_number", nullable = false, length = 50, unique = true)
    private String organizationNumber;


    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();

    /**
     * Timestamp when the organization was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the creation timestamp before persisting a new organization.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}