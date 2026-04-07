package backend.fullstack.readings.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import backend.fullstack.organization.Organization;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "temperature_readings",
        indexes = {
                @Index(name = "idx_readings_unit_id", columnList = "unit_id"),
                @Index(name = "idx_readings_organization_id", columnList = "organization_id"),
                @Index(name = "idx_readings_recorded_at", columnList = "recorded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemperatureReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private TemperatureUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "is_out_of_range", nullable = false)
    private boolean isOutOfRange;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getUnitId() {
        return unit != null ? unit.getId() : null;
    }

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getRecordedByName() {
        return recordedBy != null ? recordedBy.getFirstName() + " " + recordedBy.getLastName() : null;
    }
}
