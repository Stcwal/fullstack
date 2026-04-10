package backend.fullstack.deviations.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import backend.fullstack.location.Location;
import backend.fullstack.organization.Organization;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "deviations",
        indexes = {
                @Index(name = "idx_deviations_organization_id", columnList = "organization_id"),
        @Index(name = "idx_deviations_status", columnList = "status"),
        @Index(name = "idx_deviations_severity", columnList = "severity"),
        @Index(name = "idx_deviations_module_type", columnList = "module_type"),
        @Index(name = "idx_deviations_related_reading_id", columnList = "related_reading_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deviation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeviationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private DeviationSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", nullable = false, length = 30)
    private DeviationModuleType moduleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_reading_id")
    private TemperatureReading relatedReading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution", length = 2000)
    private String resolution;

    @OneToMany(
            mappedBy = "deviation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<DeviationComment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getReportedByName() {
        return reportedBy != null ? reportedBy.getFirstName() + " " + reportedBy.getLastName() : null;
    }

    public String getResolvedByName() {
        return resolvedBy != null ? resolvedBy.getFirstName() + " " + resolvedBy.getLastName() : null;
    }

    public Long getRelatedReadingId() {
        return relatedReading != null ? relatedReading.getId() : null;
    }

    public String getLocationName() {
        return location != null ? location.getName() : null;
    }

    public void addComment(DeviationComment comment) {
        comments.add(comment);
        comment.setDeviation(this);
    }
}
