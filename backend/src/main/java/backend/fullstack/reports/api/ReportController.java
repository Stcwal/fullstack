package backend.fullstack.reports.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.reports.api.dto.ChartResponse;
import backend.fullstack.reports.application.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Analytics and chart data")
@SecurityRequirement(name = "Bearer Auth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(
            summary = "Get chart data",
            description = "Returns daily temperature averages per unit for the given period (WEEK or MONTH)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chart data retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ChartResponse>> getChartData(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam(required = false) Long locationId) {
        Long resolvedLocationId = resolveLocationId(principal, locationId);
        ChartResponse response = reportService.getChartData(principal.organizationId(), period, resolvedLocationId);
        return ResponseEntity.ok(ApiResponse.success("Chart data retrieved", response));
    }

    private Long resolveLocationId(JwtPrincipal principal, Long requestedLocationId) {
        if (requestedLocationId != null) return requestedLocationId;
        java.util.List<Long> ids = principal.locationIds();
        return ids.isEmpty() ? null : ids.get(0);
    }
}
