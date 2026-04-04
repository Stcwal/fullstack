package backend.fullstack.checklist.api.dto;

import java.time.Instant;

public record ChecklistInstanceItemResponse(
        Long id,
        String text,
        boolean completed,
        CompletedByResponse completedBy,
        Instant completedAt
) {
}
