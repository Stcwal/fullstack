package backend.fullstack.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload used when accepting an invite and setting initial password.
 */
@Getter
@Setter
@Schema(description = "Accept invite request payload")
public class AcceptInviteRequest {

    @NotBlank(message = "Invite token is required")
    @Schema(description = "One-time invite token from email link")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    @Schema(description = "New account password", example = "P@ssw0rd1")
    private String password;
}
