package backend.fullstack.units.api.dto;

import jakarta.validation.constraints.NotNull;

public record UnitStatusRequest(
        @NotNull(message = "Active flag is required")
        Boolean active
) {
}
