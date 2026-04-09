package backend.fullstack.export.api.dto;

import java.time.LocalDate;

import backend.fullstack.export.domain.ExportFormat;
import backend.fullstack.export.domain.ExportModule;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for data export.
 *
 * @param module  the data module to export
 * @param format  desired output format (PDF or JSON)
 * @param from    optional start date filter (inclusive)
 * @param to      optional end date filter (inclusive)
 */
public record ExportRequest(
        @NotNull(message = "Module is required")
        ExportModule module,

        @NotNull(message = "Format is required")
        ExportFormat format,

        LocalDate from,

        LocalDate to
) {}
