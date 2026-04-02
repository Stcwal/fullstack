package backend.fullstack.checklist.api.dto;

import jakarta.validation.constraints.NotNull;

public record ChecklistInstanceItemUpdateRequest(
        @NotNull Boolean completed
) {
}
