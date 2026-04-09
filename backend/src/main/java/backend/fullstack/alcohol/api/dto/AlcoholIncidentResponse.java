package backend.fullstack.alcohol.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.alcohol.domain.IncidentSeverity;
import backend.fullstack.alcohol.domain.IncidentStatus;
import backend.fullstack.alcohol.domain.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Alcohol serving incident response")
public class AlcoholIncidentResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "100")
    private Long organizationId;

    @Schema(example = "1")
    private Long locationId;

    @Schema(example = "Bar")
    private String locationName;

    @Schema(example = "5")
    private Long reportedByUserId;

    @Schema(example = "Ola Nordmann")
    private String reportedByName;

    @Schema(example = "8")
    private Long resolvedByUserId;

    @Schema(example = "Kari Nordmann")
    private String resolvedByName;

    @Schema(example = "REFUSED_SERVICE")
    private IncidentType incidentType;

    @Schema(example = "MEDIUM")
    private IncidentSeverity severity;

    @Schema(example = "OPEN")
    private IncidentStatus status;

    private String description;

    private String correctiveAction;

    private LocalDateTime occurredAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
