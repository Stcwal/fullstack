package backend.fullstack.location;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.location.dto.LocationRequest;
import backend.fullstack.location.dto.LocationResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for managing locations within an organization.
 * Endpoints:
 * - POST /api/locations: Create a new location.
 * - GET /api/locations: Get all accessible locations.
 * - GET /api/locations/{id}: Get location by ID.
 * - PUT /api/locations/{id}: Update location.
 * - DELETE /api/locations/{id}: Delete location.
 * 
 * @version 1.0
 * @since 03.04.26
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @Operation(summary = "Create a new location")
    public ApiResponse<LocationResponse> create(@Valid @RequestBody LocationRequest request) {
        return ApiResponse.success("Location created", locationService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all accessible locations")
    public ApiResponse<List<LocationResponse>> getAllAccessible() {
        return ApiResponse.success("Accessible locations retrieved", locationService.getAllAccessible());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID")
    public ApiResponse<LocationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Location retrieved", locationService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update location")
    public ApiResponse<LocationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest request
    ) {
        return ApiResponse.success("Location updated", locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ApiResponse.success("Location deleted", null);
    }
}
