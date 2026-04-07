package backend.fullstack.temperature.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TemperatureReadingRequest(
        @NotNull(message = "Temperature is required")
        @DecimalMin(value = "-273.15", message = "Temperature must be greater than or equal to -273.15")
        Double temperature,

        LocalDateTime recordedAt,

        @Size(max = 500, message = "Note must be at most 500 characters")
        String note
) {
}