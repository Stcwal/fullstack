package backend.fullstack.user.role;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    // Required when promoting to MANAGER or STAFF
    private Long locationId;
}