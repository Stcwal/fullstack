package backend.fullstack.deviations.api.dto;

import jakarta.validation.constraints.Size;

public record ResolveDeviationRequest(
        @Size(max = 2000) String resolution
) {}
