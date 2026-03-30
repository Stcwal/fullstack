package backend.fullstack.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Location request.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Getter
@Setter
@Schema(description = "LocationRequest information")
public class LocationRequest {

    @NotBlank(message = "Location name is required")
    @Schema(description = "Location name", example = "Trondheim Office")
    private String name;

    @NotBlank(message = "Location address is required")
    @Schema(description = "Location address", example = "Kongens gate 1, 7011 Trondheim")
    private String address;
}
