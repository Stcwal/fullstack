package backend.fullstack.units.api.dto;

import backend.fullstack.units.domain.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnitRequest(
        @NotBlank(message = "Unit name is required")
        @Size(max = 120, message = "Unit name must be at most 120 characters")
        String name,

        @NotNull(message = "Unit type is required")
        UnitType type,

        @NotNull(message = "Target temperature is required")
        Double targetTemperature,

        @NotNull(message = "Minimum threshold is required")
        Double minThreshold,

        @NotNull(message = "Maximum threshold is required")
        Double maxThreshold,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
