package backend.fullstack.deviations.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.deviations.domain.DeviationModuleType;
import backend.fullstack.deviations.domain.DeviationSeverity;
import backend.fullstack.deviations.domain.DeviationStatus;

public record DeviationResponse(
        Long id,
        String title,
        String description,
        DeviationStatus status,
        DeviationSeverity severity,
        DeviationModuleType moduleType,
        String reportedBy,
        LocalDateTime reportedAt,
        String resolvedBy,
        LocalDateTime resolvedAt,
        String resolution,
        String locationName
) {}
