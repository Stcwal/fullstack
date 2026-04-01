package backend.fullstack.user.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for assigning permission profiles to a user.
 * 
 * @version 1.0
 * @since 31.03.26
 */
@Getter
@Setter
@Schema(description = "Assign permission profiles to user")
public class AssignProfilesRequest {

    @NotEmpty(message = "At least one profile id is required")
    private List<Long> profileIds = new ArrayList<>();

    @Schema(description = "Optional location scope for these profile assignments", example = "2")
    private Long locationId;

    @Schema(description = "Optional activation start timestamp")
    private LocalDateTime startsAt;

    @Schema(description = "Optional activation end timestamp")
    private LocalDateTime endsAt;

    @Schema(description = "Replace existing profile assignments in this scope before adding new ones", example = "true")
    private boolean replaceScopeAssignments = true;
}
