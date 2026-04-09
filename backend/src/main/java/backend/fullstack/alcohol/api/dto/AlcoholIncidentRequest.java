package backend.fullstack.alcohol.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.alcohol.domain.IncidentSeverity;
import backend.fullstack.alcohol.domain.IncidentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to report an alcohol serving incident")
public class AlcoholIncidentRequest {

    @NotNull(message = "Location id is required")
    @Schema(description = "Location where the incident occurred", example = "1")
    private Long locationId;

    @NotNull(message = "Incident type is required")
    @Schema(description = "Type of incident", example = "REFUSED_SERVICE")
    private IncidentType incidentType;

    @NotNull(message = "Severity is required")
    @Schema(description = "Severity of the incident", example = "MEDIUM")
    private IncidentSeverity severity;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description of what happened", example = "Guest appeared intoxicated and was refused further service")
    private String description;

    @Schema(description = "When the incident occurred (defaults to now)")
    private LocalDateTime occurredAt;
}
