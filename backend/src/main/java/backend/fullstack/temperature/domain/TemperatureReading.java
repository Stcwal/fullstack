package backend.fullstack.temperature.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "temperature_readings",
        indexes = {
                @Index(name = "idx_temperature_readings_organization_id", columnList = "organization_id"),
                @Index(name = "idx_temperature_readings_unit_id", columnList = "unit_id"),
                @Index(name = "idx_temperature_readings_recorded_at", columnList = "recorded_at"),
                @Index(name = "idx_temperature_readings_is_deviation", columnList = "is_deviation")
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private TemperatureUnit unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedBy;

    @NotNull(message = "Temperature is required")
    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @NotNull(message = "Recorded time is required")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Size(max = 500, message = "Note must be at most 500 characters")
    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "is_deviation", nullable = false)
    @Builder.Default
    private boolean isDeviation = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public Long getUnitId() {
        return unit != null ? unit.getId() : null;
    }

    public Long getRecordedByUserId() {
        return recordedBy != null ? recordedBy.getId() : null;
    }

    public void evaluateDeviation(Double minThreshold, Double maxThreshold) {
        if (temperature == null || minThreshold == null || maxThreshold == null) {
            this.isDeviation = false;
            return;
        }

        this.isDeviation = temperature < minThreshold || temperature > maxThreshold;
    }
}