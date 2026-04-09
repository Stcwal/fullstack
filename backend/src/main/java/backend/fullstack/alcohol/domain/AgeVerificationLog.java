package backend.fullstack.alcohol.domain;

import java.time.LocalDateTime;

import backend.fullstack.location.Location;
import backend.fullstack.organization.Organization;
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
import org.hibernate.annotations.CreationTimestamp;

/**
 * Records an age verification check performed during alcohol service.
 * Required by Norwegian alcohol law (Alkoholloven) for compliance documentation.
 */
@Entity
@Table(name = "age_verification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verified_by_user_id", nullable = false)
    private User verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false, length = 30)
    private VerificationMethod verificationMethod;

    @Column(name = "guest_appeared_underage", nullable = false)
    @Builder.Default
    private boolean guestAppearedUnderage = true;

    @Column(name = "id_was_valid")
    private Boolean idWasValid;

    @Column(name = "was_refused", nullable = false)
    @Builder.Default
    private boolean wasRefused = false;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public Long getLocationId() {
        return location != null ? location.getId() : null;
    }

    public Long getVerifiedByUserId() {
        return verifiedBy != null ? verifiedBy.getId() : null;
    }
}
