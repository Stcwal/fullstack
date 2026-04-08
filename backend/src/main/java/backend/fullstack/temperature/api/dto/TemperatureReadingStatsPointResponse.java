package backend.fullstack.temperature.api.dto;

import java.time.LocalDateTime;

public record TemperatureReadingStatsPointResponse(
        LocalDateTime timestamp,
        Double avgTemperature,
        boolean isDeviation
) {
}