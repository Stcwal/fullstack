package backend.fullstack.auth;

import java.util.ArrayList;
import java.util.List;

import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Response payload returned after successful login.
 */
@Getter
@Setter
@Schema(description = "Login response payload")
public class LoginResponse {

	@Schema(description = "Authenticated user id", example = "42")
	private Long userId;

	@Schema(description = "Authenticated user email", example = "manager@everest.no")
	private String email;

	@Schema(description = "Authenticated user role", example = "MANAGER")
	private Role role;

	@Schema(description = "Organization id", example = "1")
	private Long organizationId;

	@Schema(description = "Home location id for MANAGER/STAFF", example = "1", nullable = true)
	private Long primaryLocationId;

	@Schema(description = "All locations the user can access", example = "[1,2,3]")
	private List<Long> allowedLocationIds = new ArrayList<>();

	@Schema(description = "First name", example = "Kari")
	private String firstName;

	@Schema(description = "Last name", example = "Larsen")
	private String lastName;

	@Schema(description = "JWT token for Bearer auth fallback", example = "eyJ...")
	private String token;

}
