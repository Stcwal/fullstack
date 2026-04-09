package backend.fullstack.alcohol.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.alcohol.api.dto.AlcoholLicenseRequest;
import backend.fullstack.alcohol.api.dto.AlcoholLicenseResponse;
import backend.fullstack.alcohol.application.AlcoholLicenseService;
import backend.fullstack.config.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST endpoints for managing alcohol licenses (bevillinger).
 */
@RestController
@RequestMapping("/api/alcohol/licenses")
@Tag(name = "Alcohol Licenses", description = "Manage organization alcohol licenses (bevillinger)")
@SecurityRequirement(name = "Bearer Auth")
public class AlcoholLicenseController {

    private final AlcoholLicenseService licenseService;

    public AlcoholLicenseController(AlcoholLicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "List all alcohol licenses for the organization")
    public ApiResponse<List<AlcoholLicenseResponse>> list() {
        return ApiResponse.success("Licenses fetched", licenseService.listLicenses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(summary = "Get alcohol license by id")
    public ApiResponse<AlcoholLicenseResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("License fetched", licenseService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new alcohol license")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "License created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<AlcoholLicenseResponse>> create(
            @Valid @RequestBody AlcoholLicenseRequest request
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success("License created", licenseService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an alcohol license")
    public ApiResponse<AlcoholLicenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AlcoholLicenseRequest request
    ) {
        return ApiResponse.success("License updated", licenseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an alcohol license")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        licenseService.delete(id);
        return ApiResponse.success("License deleted", null);
    }
}
