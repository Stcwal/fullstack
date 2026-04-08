package backend.fullstack.temperature.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.temperature.api.dto.TemperatureReadingRequest;
import backend.fullstack.temperature.api.dto.TemperatureReadingResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsGroupBy;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsResponse;
import backend.fullstack.temperature.application.TemperatureReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Temperature Readings", description = "Temperature logging endpoints")
@SecurityRequirement(name = "Bearer Auth")
public class TemperatureReadingController {

    private final TemperatureReadingService readingService;

    public TemperatureReadingController(TemperatureReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/units/{unitId}/readings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "List readings for a unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Readings retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<TemperatureReadingResponse>>> getUnitReadings(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<TemperatureReadingResponse> readings =
                readingService.listUnitReadings(principal.organizationId(), unitId, from, to);
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", readings));
    }

    @GetMapping("/readings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "List readings with filters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Readings retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<TemperatureReadingResponse>>> getReadings(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean deviationsOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordedAt"));
        Page<TemperatureReadingResponse> readings = readingService.listReadings(
                principal.organizationId(),
                unitId,
                from,
                to,
                deviationsOnly,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", readings));
    }

    @GetMapping("/readings/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get reading statistics for charts")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reading statistics retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<TemperatureReadingStatsResponse>> getReadingStats(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) List<Long> unitIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "DAY") TemperatureReadingStatsGroupBy groupBy
    ) {
        TemperatureReadingStatsResponse response = readingService.getReadingStats(
                principal.organizationId(),
                unitIds,
                from,
                to,
                groupBy
        );

        return ResponseEntity.ok(ApiResponse.success("Reading statistics retrieved", response));
    }

    @PostMapping("/units/{unitId}/readings")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Register temperature reading")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reading created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Unit inactive")
    })
    public ResponseEntity<ApiResponse<TemperatureReadingResponse>> createReading(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long unitId,
            @Valid @RequestBody TemperatureReadingRequest request
    ) {
        TemperatureReadingResponse response = readingService.createReading(principal, unitId, request);
        return ResponseEntity.status(201).body(ApiResponse.success("Reading created", response));
    }
}