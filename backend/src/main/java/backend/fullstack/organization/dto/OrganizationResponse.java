package backend.fullstack.organization.dto;

import backend.fullstack.location.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for Organization response.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Setter
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "OrganizationResponse information")
public class OrganizationResponse {

    @Schema(description = "Organization ID", example = "1")
    private Long id;

    @Schema(description = "Organization name", example = "Tech Company")
    private String name;

    @Schema(description = "Organization number", example = "123456789")
    private int organizationNumber;

    @Schema(description = "Number of locations associated with the organization", example = "3")
    private int locationCount;

    @Schema(description = "List of locations associated with the organization", example = "trondheim, oslo")
    private List<Location> locationsList;
}
