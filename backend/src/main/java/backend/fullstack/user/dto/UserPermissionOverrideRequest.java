package backend.fullstack.user.dto;

import java.time.LocalDateTime;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for assigning a user-specific permission override. This allows granting or
 * denying specific permissions to a user, with optional scoping and time limits.
 * 
 * @version 1.0
 * @since 31.03.26
 */
@Getter
@Setter
@Schema(description = "Assign user-specific permission override")
public class UserPermissionOverrideRequest {

    @NotNull(message = "permission is required")
    private Permission permission;

    @NotNull(message = "effect is required")
    private PermissionEffect effect;

    private PermissionScope scope = PermissionScope.ORGANIZATION;

    private Long locationId;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private String reason;
}
