package backend.fullstack.user.dto;

import backend.fullstack.user.role.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for User response.
 *
 * @version 1.0
 * @since 30.03.26
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "UserResponse information")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's first name", example = "John ")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "User's email", example = "stian@stian.no")
    @Email
    private String email;

    @Schema(description = "User's role", example = "STAFF")
    private Role role;

    @Schema(description = "User's home location ID", example = "1")
    private Long homeLocationId;

    @Schema(description = "User's home location name", example = "Trondheim branch")
    private String homeLocationName;

    @Schema(description = "Additional location IDs this user can access", example = "[1, 2, 3]")
    private List<Long> additionalLocationIds;

    @Schema(description = "User's organization name", example = "Everest Sushi AS")
    private String organizationName;

    @Schema(description = "User's organization ID", example = "123123123")
    private Long organizationId;

    @Schema(description = "Whether the user account is active", example = "true")
    private boolean isActive;

    @Schema(description = "When the user was created")
    private LocalDateTime createdAt;
}
