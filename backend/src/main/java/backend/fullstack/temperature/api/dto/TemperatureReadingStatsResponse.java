package backend.fullstack.temperature.api.dto;

import java.util.List;

public record TemperatureReadingStatsResponse(
        List<TemperatureReadingStatsSeriesResponse> series,
        List<TemperatureReadingDeviationResponse> deviations
) {
}