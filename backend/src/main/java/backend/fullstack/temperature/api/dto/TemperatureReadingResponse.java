package backend.fullstack.temperature.api.dto;

import java.time.LocalDateTime;

public record TemperatureReadingResponse(
        Long id,
        Long organizationId,
        Long unitId,
        String unitName,
        Double temperature,
        Double targetTemperature,
        Double minThreshold,
        Double maxThreshold,
        boolean isDeviation,
        LocalDateTime recordedAt,
        String note,
        RecordedByResponse recordedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}