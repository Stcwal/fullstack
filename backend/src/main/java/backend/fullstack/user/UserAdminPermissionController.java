package backend.fullstack.user;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.user.dto.AssignProfilesRequest;
import backend.fullstack.user.dto.TemporaryLocationScopeRequest;
import backend.fullstack.user.dto.UserPermissionOverrideRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

/**
 * Controller for admin operations related to user permissions and profiles.
 * 
 * Endpoints:
 * - PUT /api/admin/users/{id}/profiles: Assign permission profiles to a user.
 * - POST /api/admin/users/{id}/permissions: Assign user-specific permission override.
 * - POST /api/admin/users/{id}/locations/temporary: Assign temporary location scope to a user.
 * 
 * @version 1.0
 * @since 31.03.26
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserAdminPermissionController {

    private final UserService userService;

    public UserAdminPermissionController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/profiles")
    @Operation(summary = "Assign profiles to user")
    public ApiResponse<Void> assignProfiles(
            @PathVariable Long id,
            @Valid @RequestBody AssignProfilesRequest request
    ) {
        userService.assignProfiles(
            id,
            request.getProfileIds(),
            request.getLocationId(),
            request.getStartsAt(),
            request.getEndsAt(),
            request.isReplaceScopeAssignments()
        );
        return ApiResponse.success("Profiles assigned", null);
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "Assign user-specific permission override")
    public ApiResponse<Void> assignPermissionOverride(
            @PathVariable Long id,
            @Valid @RequestBody UserPermissionOverrideRequest request
    ) {
        userService.assignTemporaryPermission(
                id,
                request.getPermission(),
                request.getEffect(),
                request.getScope(),
                request.getLocationId(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getReason()
        );
        return ApiResponse.success("Permission override assigned", null);
    }

    @PostMapping("/{id}/locations/temporary")
    @Operation(summary = "Assign temporary location scope")
    public ApiResponse<Void> assignTemporaryLocation(
            @PathVariable Long id,
            @Valid @RequestBody TemporaryLocationScopeRequest request
    ) {
        userService.assignTemporaryLocationScope(
                id,
                request.getLocationId(),
                request.getStartsAt(),
                request.getEndsAt(),
                request.getMode(),
                request.getReason()
        );
        return ApiResponse.success("Temporary location scope assigned", null);
    }

    @PostMapping("/{id}/locations/temporary/{assignmentId}/complete")
    @Operation(summary = "Complete temporary location assignment")
    public ApiResponse<Void> completeTemporaryLocation(
            @PathVariable Long id,
            @PathVariable Long assignmentId
    ) {
        userService.completeTemporaryLocationScope(id, assignmentId);
        return ApiResponse.success("Temporary location assignment completed", null);
    }

    @PostMapping("/{id}/locations/temporary/{assignmentId}/confirm")
    @Operation(summary = "Confirm temporary location assignment completion")
    public ApiResponse<Void> confirmTemporaryLocation(
            @PathVariable Long id,
            @PathVariable Long assignmentId
    ) {
        userService.confirmTemporaryLocationScope(id, assignmentId);
        return ApiResponse.success("Temporary location assignment confirmed", null);
    }
}
