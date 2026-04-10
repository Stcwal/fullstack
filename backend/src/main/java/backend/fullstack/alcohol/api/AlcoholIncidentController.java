package backend.fullstack.alcohol.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.alcohol.api.dto.AlcoholIncidentRequest;
import backend.fullstack.alcohol.api.dto.AlcoholIncidentResponse;
import backend.fullstack.alcohol.api.dto.ResolveIncidentRequest;
import backend.fullstack.alcohol.application.AlcoholIncidentService;
import backend.fullstack.alcohol.domain.IncidentStatus;
import backend.fullstack.alcohol.domain.IncidentType;
import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST endpoints for managing alcohol-related serving incidents.
 */
@RestController
@RequestMapping("/api/alcohol/incidents")
@Tag(name = "Alcohol Incidents", description = "Report and manage alcohol serving incidents")
@SecurityRequirement(name = "Bearer Auth")
public class AlcoholIncidentController {

    private final AlcoholIncidentService incidentService;

    public AlcoholIncidentController(AlcoholIncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Report a new alcohol serving incident")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Incident reported"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<AlcoholIncidentResponse>> create(
            @Valid @RequestBody AlcoholIncidentRequest request
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Incident reported", incidentService.create(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "List alcohol incidents with optional filters")
    public ApiResponse<List<AlcoholIncidentResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentType incidentType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        Long resolvedLocation = resolveLocationId(principal, locationId);
        return ApiResponse.success("Incidents fetched", incidentService.list(resolvedLocation, status, incidentType, from, to));
    }

    private Long resolveLocationId(JwtPrincipal principal, Long requestedLocationId) {
        if (requestedLocationId != null) return requestedLocationId;
        if (principal.role() == Role.ADMIN || principal.role() == Role.SUPERVISOR) return null;
        java.util.List<Long> ids = principal.locationIds();
        return ids.isEmpty() ? null : ids.get(0);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "Get alcohol incident by id")
    public ApiResponse<AlcoholIncidentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Incident fetched", incidentService.getById(id));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "Resolve an alcohol incident with corrective action")
    public ApiResponse<AlcoholIncidentResponse> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveIncidentRequest request
    ) {
        return ApiResponse.success("Incident resolved", incidentService.resolve(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(summary = "Update incident status (e.g. close an incident)")
    public ApiResponse<AlcoholIncidentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status
    ) {
        return ApiResponse.success("Incident status updated", incidentService.updateStatus(id, status));
    }
}
