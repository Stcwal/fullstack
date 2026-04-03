package backend.fullstack.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.location.dto.AssignLocationsRequest;
import backend.fullstack.user.dto.ChangePasswordRequest;
import backend.fullstack.user.dto.CreateUserRequest;
import backend.fullstack.user.dto.UpdateUserProfileRequest;
import backend.fullstack.user.dto.UserResponse;
import backend.fullstack.user.role.UpdateRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for core user management operations.
 *
 * This controller handles the main user lifecycle within the caller's
 * organization, while advanced permission/profile assignment lives in
 * UserAdminPermissionController.
 * 
 * Endpoints:
 * - GET /api/users: Get all users in current organization.
 * - GET /api/users/me: Get current user profile.
 * - GET /api/users/{id}: Get user by ID.
 * - POST /api/users: Create a new user.
 * - PUT /api/users/{id}: Update user profile.
 * - PUT /api/users/{id}/role: Update user role.
 * - PUT /api/users/{id}/locations: Assign additional user locations.
 * - POST /api/users/me/change-password: Change current user password.
 * - POST /api/users/{id}/deactivate: Deactivate user.
 * - POST /api/users/{id}/reactivate: Reactivate user.
 * 
 * @version 1.0
 * @since 03.04.26
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get users in current organization")
    public ApiResponse<List<UserResponse>> getAllInOrganization() {
        return ApiResponse.success("Users retrieved", userService.getAllInOrganization());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.success("Profile retrieved", userService.getMyProfile());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("User retrieved", userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success("User created", userService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ApiResponse<UserResponse> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return ApiResponse.success("User updated", userService.updateProfile(id, request));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role")
    public ApiResponse<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ApiResponse.success("User role updated", userService.updateRole(id, request));
    }

    @PutMapping("/{id}/locations")
    @Operation(summary = "Assign additional user locations")
    public ApiResponse<UserResponse> assignAdditionalLocations(
            @PathVariable Long id,
            @Valid @RequestBody AssignLocationsRequest request
    ) {
        return ApiResponse.success("User locations updated", userService.assignAdditionalLocations(id, request));
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change current user password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success("Password changed", null);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ApiResponse.success("User deactivated", null);
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ApiResponse.success("User reactivated", null);
    }
}
