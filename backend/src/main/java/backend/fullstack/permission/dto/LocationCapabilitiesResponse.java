package backend.fullstack.permission.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Effective capabilities at a specific location.
 */
@Getter
@Setter
public class LocationCapabilitiesResponse {

    @Schema(description = "Location id", example = "3")
    private Long locationId;

    @Schema(description = "Flattened effective permission keys for this location")
    private List<String> permissions = new ArrayList<>();
}
