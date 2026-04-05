package backend.fullstack.units.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.units.api.dto.UnitRequest;
import backend.fullstack.units.api.dto.UnitResponse;
import backend.fullstack.units.api.dto.UnitStatusRequest;
import backend.fullstack.units.application.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/units")
@Tag(name = "Units", description = "Temperature units management")
@SecurityRequirement(name = "Bearer Auth")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Get units", description = "Returns all units in authenticated organization")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Units retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getUnits(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) Boolean active
    ) {
        List<UnitResponse> units = unitService.getUnits(principal.organizationId(), active);
        return ResponseEntity.ok(ApiResponse.success("Units retrieved", units));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(summary = "Get unit by id")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<UnitResponse>> getUnit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        UnitResponse unit = unitService.getUnit(principal.organizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Unit retrieved", unit));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Unit created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UnitRequest request
    ) {
        UnitResponse unit = unitService.createUnit(principal.organizationId(), request);
        return ResponseEntity.status(201).body(ApiResponse.success("Unit created", unit));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UnitRequest request
    ) {
        UnitResponse unit = unitService.updateUnit(principal.organizationId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Unit updated", unit));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update unit status", description = "Sets active flag for a unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnitStatus(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UnitStatusRequest request
    ) {
        UnitResponse unit = unitService.updateStatus(principal.organizationId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Unit status updated", unit));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete unit")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unit soft-deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Unit not found")
    })
    public ResponseEntity<ApiResponse<UnitResponse>> deleteUnit(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        UnitResponse unit = unitService.softDelete(principal.organizationId(), id);
        return ResponseEntity.ok(ApiResponse.success("Unit soft-deleted", unit));
    }
}
