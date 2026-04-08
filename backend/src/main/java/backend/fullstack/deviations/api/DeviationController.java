package backend.fullstack.deviations.api;

import java.util.List;

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

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.deviations.api.dto.DeviationCommentRequest;
import backend.fullstack.deviations.api.dto.DeviationCommentResponse;
import backend.fullstack.deviations.api.dto.DeviationRequest;
import backend.fullstack.deviations.api.dto.DeviationResponse;
import backend.fullstack.deviations.api.dto.ResolveDeviationRequest;
import backend.fullstack.deviations.api.dto.UpdateDeviationStatusRequest;
import backend.fullstack.deviations.application.DeviationService;
import backend.fullstack.deviations.domain.DeviationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/deviations")
@Tag(name = "Deviations", description = "Deviation reports management")
@SecurityRequirement(name = "Bearer Auth")
public class DeviationController {

    private final DeviationService deviationService;

    public DeviationController(DeviationService deviationService) {
        this.deviationService = deviationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Get deviations", description = "Returns all deviations for the authenticated organization, optionally filtered by status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deviations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<DeviationResponse>>> getDeviations(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) DeviationStatus status) {
        List<DeviationResponse> deviations = deviationService.getDeviations(principal.organizationId(), status);
        return ResponseEntity.ok(ApiResponse.success("Deviations retrieved", deviations));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Create deviation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Deviation created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<DeviationResponse>> createDeviation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody DeviationRequest request) {
        DeviationResponse deviation = deviationService.createDeviation(principal.organizationId(), principal.userId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success("Deviation created", deviation));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "Resolve a deviation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deviation resolved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deviation not found")
    })
    public ResponseEntity<ApiResponse<DeviationResponse>> resolveDeviation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ResolveDeviationRequest request) {
        DeviationResponse deviation = deviationService.resolveDeviation(principal.organizationId(), principal.userId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Deviation resolved", deviation));
    }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
        @Operation(summary = "Update deviation status")
        @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deviation status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deviation not found")
        })
        public ResponseEntity<ApiResponse<DeviationResponse>> updateDeviationStatus(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDeviationStatusRequest request
        ) {
        DeviationResponse deviation = deviationService.updateDeviationStatus(
            principal.organizationId(),
            principal.userId(),
            id,
            request
        );
        return ResponseEntity.ok(ApiResponse.success("Deviation status updated", deviation));
        }

        @PostMapping("/{id}/comments")
        @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
        @Operation(summary = "Add comment to deviation")
        @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Deviation not found")
        })
        public ResponseEntity<ApiResponse<DeviationCommentResponse>> addDeviationComment(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody DeviationCommentRequest request
        ) {
        DeviationCommentResponse response = deviationService.addComment(
            principal.organizationId(),
            principal.userId(),
            id,
            request
        );
        return ResponseEntity.status(201).body(ApiResponse.success("Deviation comment created", response));
        }
}
