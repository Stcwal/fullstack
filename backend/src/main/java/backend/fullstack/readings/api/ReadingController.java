package backend.fullstack.readings.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.readings.api.dto.ReadingRequest;
import backend.fullstack.readings.api.dto.ReadingResponse;
import backend.fullstack.readings.application.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/units/{unitId}/readings")
@Tag(name = "Readings", description = "Temperature readings for storage units")
@SecurityRequirement(name = "Bearer Auth")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Get readings for a unit", description = "Returns all temperature readings for a unit in the authenticated organization")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Readings retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> getReadings(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long unitId) {
        List<ReadingResponse> readings = readingService.getReadingsForUnit(principal.organizationId(), unitId);
        return ResponseEntity.ok(ApiResponse.success("Readings retrieved", readings));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Record a temperature reading", description = "Records a new temperature reading for the specified unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reading recorded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<ReadingResponse>> recordReading(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long unitId,
            @Valid @RequestBody ReadingRequest request) {
        ReadingResponse reading = readingService.recordReading(
                principal.organizationId(), principal.userId(), unitId, request);
        return ResponseEntity.status(201).body(ApiResponse.success("Reading recorded", reading));
    }
}
