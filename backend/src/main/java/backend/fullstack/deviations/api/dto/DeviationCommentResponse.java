package backend.fullstack.deviations.api.dto;

import java.time.LocalDateTime;

public record DeviationCommentResponse(
        Long id,
        String comment,
        Long createdById,
        String createdBy,
        LocalDateTime createdAt
) {}