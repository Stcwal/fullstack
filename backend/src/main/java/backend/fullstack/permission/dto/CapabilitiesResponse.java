package backend.fullstack.permission.dto;

import java.util.ArrayList;
import java.util.List;

import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO representing the effective capabilities of the currently authenticated user, including their role, organization context, accessible locations, and flattened permissions. This is used to inform frontend applications about what actions the user can perform and what UI elements to show or hide based on their permissions.
 * 
 * @version 1.0
 * @since 30.03.26
 */
@Getter
@Setter
@Schema(description = "Effective capabilities for the currently authenticated user")
public class CapabilitiesResponse {

    @Schema(description = "Current role", example = "SUPERVISOR")
    private Role role;

    @Schema(description = "Current organization id", example = "1")
    private Long organizationId;

    @Schema(description = "Locations the user can access", example = "[1,2,3]")
    private List<Long> allowedLocationIds = new ArrayList<>();

    @Schema(description = "Flattened effective permission keys", example = "[\"users.read\",\"temperature.log\"]")
    private List<String> permissions = new ArrayList<>();

    @Schema(description = "Roles this actor can manage", example = "[\"MANAGER\",\"STAFF\"]")
    private List<Role> manageableRoles = new ArrayList<>();

    @Schema(description = "Location ids this actor can assign to managed users", example = "[1,2]")
    private List<Long> manageableLocationIds = new ArrayList<>();
}