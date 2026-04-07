package backend.fullstack.readings.api.dto;

import java.time.LocalDateTime;

public record ReadingResponse(
        Long id,
        Long unitId,
        Double temperature,
        LocalDateTime recordedAt,
        String recordedBy,
        String note,
        boolean isOutOfRange
) {
}
