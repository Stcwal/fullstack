package backend.fullstack.auth;

import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Bootstrap registration payload")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Admin email", example = "admin@everest.no")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    @Schema(description = "Password", example = "Admin123!")
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Ola")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Nordmann")
    private String lastName;

    @NotNull(message = "Organization id is required")
    @Schema(description = "Organization id", example = "1")
    private Long organizationId;

    @Schema(description = "Primary location id", example = "1", nullable = true)
    private Long primaryLocationId;

    @NotNull(message = "Role is required")
    @Schema(description = "Role must be ADMIN for bootstrap", example = "ADMIN")
    private Role role;
}
