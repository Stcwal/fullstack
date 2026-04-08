package backend.fullstack.temperature.api.dto;

import java.time.LocalDateTime;

public record TemperatureReadingDeviationResponse(
        Long id,
        Long unitId,
        String unitName,
        Double temperature,
        Double threshold,
        LocalDateTime timestamp
) {
}