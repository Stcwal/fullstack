package backend.fullstack.temperature.api.dto;

import java.util.List;

public record TemperatureReadingStatsSeriesResponse(
        Long unitId,
        String unitName,
        List<TemperatureReadingStatsPointResponse> dataPoints
) {
}