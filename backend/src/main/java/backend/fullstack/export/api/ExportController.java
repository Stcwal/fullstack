package backend.fullstack.export.api;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.export.api.dto.ExportRequest;
import backend.fullstack.export.application.ExportService;
import backend.fullstack.export.domain.ExportFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for exporting data as PDF or JSON.
 */
@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "Data export in PDF and JSON formats")
@SecurityRequirement(name = "Bearer Auth")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(
            summary = "Export data",
            description = "Exports data from the specified module in PDF or JSON format. "
                    + "Supports optional date range filtering."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden — missing REPORTS_EXPORT permission")
    })
    public ResponseEntity<byte[]> exportData(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ExportRequest request
    ) {
        byte[] data = exportService.export(
                principal.organizationId(),
                request.module(),
                request.format(),
                request.from(),
                request.to()
        );

        String filename = exportService.buildFilename(request.module(), request.format());
        MediaType mediaType = request.format() == ExportFormat.PDF
                ? MediaType.APPLICATION_PDF
                : MediaType.APPLICATION_JSON;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );

        return ResponseEntity.ok().headers(headers).body(data);
    }
}
