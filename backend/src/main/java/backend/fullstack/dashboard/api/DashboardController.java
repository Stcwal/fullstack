package backend.fullstack.dashboard.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.user.role.Role;
import backend.fullstack.dashboard.api.dto.DashboardResponse;
import backend.fullstack.dashboard.application.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard summary")
@SecurityRequirement(name = "Bearer Auth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Get dashboard summary", description = "Returns stats, today's checklist tasks, and open deviation alerts")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Long locationId) {
        Long resolvedLocation = resolveLocationId(principal, locationId);
        DashboardResponse response = dashboardService.getDashboard(principal.organizationId(), resolvedLocation);
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved", response));
    }

    private Long resolveLocationId(JwtPrincipal principal, Long requestedLocationId) {
        if (requestedLocationId != null) return requestedLocationId;
        if (principal.role() == Role.ADMIN || principal.role() == Role.SUPERVISOR) return null;
        java.util.List<Long> ids = principal.locationIds();
        return ids.isEmpty() ? null : ids.get(0);
    }
}
