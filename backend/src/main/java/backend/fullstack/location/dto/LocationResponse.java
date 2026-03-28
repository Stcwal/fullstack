package backend.fullstack.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Location response.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@Schema(description = "LocationResponse information")
public class LocationResponse {

    @Schema(description = "Location ID", example = "1")
    private Long id;

    @Schema(description = "Location name", example = "Trondheim Office")
    private String name;

    @Schema(description = "Location address", example = "Kongens gate 1, 7011 Trondheim")
    private String address;
}
