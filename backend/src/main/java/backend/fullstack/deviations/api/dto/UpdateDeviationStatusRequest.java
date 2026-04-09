package backend.fullstack.deviations.api.dto;

import backend.fullstack.deviations.domain.DeviationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDeviationStatusRequest(
        @NotNull(message = "Status is required")
        DeviationStatus status,

        @Size(max = 2000, message = "Resolution must be at most 2000 characters")
        String resolution
) {}