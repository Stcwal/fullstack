package backend.fullstack.readings.api.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReadingRequest(
        @NotNull Double temperature,
        @NotNull @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime recordedAt,
        @Size(max = 500) String note
) {
}
