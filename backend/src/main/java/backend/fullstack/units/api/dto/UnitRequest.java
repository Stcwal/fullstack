package backend.fullstack.units.api.dto;

import backend.fullstack.units.api.validation.ValidTemperatureThresholds;
import backend.fullstack.units.domain.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ValidTemperatureThresholds
public record UnitRequest(
        @NotBlank(message = "Unit name is required")
        @Size(max = 120, message = "Unit name must be at most 120 characters")
        String name,

        @NotNull(message = "Unit type is required")
        UnitType type,

        @NotNull(message = "Target temperature is required")
        @DecimalMin(value = "-273.15", message = "Target temperature must be greater than or equal to -273.15")
        Double targetTemperature,

        @NotNull(message = "Minimum threshold is required")
        @DecimalMin(value = "-273.15", message = "Minimum threshold must be greater than or equal to -273.15")
        Double minThreshold,

        @NotNull(message = "Maximum threshold is required")
        @DecimalMin(value = "-273.15", message = "Maximum threshold must be greater than or equal to -273.15")
        Double maxThreshold,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
