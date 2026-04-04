package backend.fullstack.organization;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for managing organizations.
 * Endpoints:
 * - POST /api/organization: Create a new organization.
 * - GET /api/organization/me: Get current organization.
 * - GET /api/organization/{id}: Get organization by ID.
 * - PUT /api/organization/me: Update current organization.
 * 
 * @version 1.0
 * @since 03.04.26
 */
@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "Create a new organization")
    public ApiResponse<OrganizationResponse> create(
            @Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.success("Organization created", organizationService.create(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current organization")
    public ApiResponse<OrganizationResponse> getCurrentOrganization() {
        return ApiResponse.success(
                "Organization retrieved",
                organizationService.getCurrentOrganization()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ApiResponse<OrganizationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(
                "Organization retrieved",
                organizationService.getById(id)
        );
    }

    @PutMapping("/me")
    @Operation(summary = "Update current organization")
    public ApiResponse<OrganizationResponse> updateCurrentOrganization(
            @Valid @RequestBody OrganizationRequest request) {
        return ApiResponse.success(
                "Organization updated",
                organizationService.updateCurrentOrganization(request)
        );
    }
}
