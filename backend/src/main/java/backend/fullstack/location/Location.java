package backend.fullstack.location;

import backend.fullstack.organization.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a physical business location belonging to one organization.
 *
 * @version 1.0
 * @since 26.03.26
 */
@Entity
@Table(
        name = "locations",
        indexes = {
                @Index(name = "idx_locations_organization_id", columnList = "organization_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Location {

    /**
     * Primary key.
     * -- GETTER --
     *  Explicit id accessor used by access-control code paths.
     *
     * @return location identifier

     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning organization (many locations can belong to one organization).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Human-readable location name.
     */
    @NotBlank(message = "Location name is required")
    @Size(max = 255, message = "Location name must be at most 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Street address.
     */
    @Size(max = 255, message = "Address must be at most 255 characters")
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Creation timestamp, set automatically on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    /**
     * Automatically sets the creation timestamp before persisting a new location.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }
}