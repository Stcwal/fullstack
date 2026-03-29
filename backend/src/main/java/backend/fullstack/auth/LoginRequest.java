package backend.fullstack.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for user login.
 */
@Getter
@Setter
@Schema(description = "Login request payload")
public class LoginRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	@Schema(description = "User email", example = "manager@everest.no")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
	@Schema(description = "User password", example = "Manager123!")
	private String password;
}
