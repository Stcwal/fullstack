package backend.fullstack.deviations.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveDeviationRequest(
        @NotBlank(message = "Resolution text is required")
        @Size(max = 2000) String resolution
) {}
