package backend.fullstack.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Organization request.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Getter
@Setter
@Schema(description = "OrganizationRequest information")
public class OrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Schema(description = "Organization name", example = "Tech Company")
    private String name;

    @NotBlank(message = "Organization number is required")
    @Pattern(regexp = "\\d{9}", message = "Organization number must be exactly 9 digits")
    @Schema(description = "Organization number", example = "123456789")
    private String organizationNumber;
}
