package backend.fullstack.user.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update a user's role")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    // Required when promoting to MANAGER or STAFF
    private Long locationId;
}