package backend.fullstack.user.dto;

import java.time.LocalDateTime;

import backend.fullstack.user.TemporaryAssignmentMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for assigning a temporary location scope to a user. This allows granting a user access to a specific location for a limited time period, which can be useful for temporary assignments
 * 
 * version 1.0
 * @since 31.03.26
 */
@Getter
@Setter
@Schema(description = "Assign temporary location scope to a user")
public class TemporaryLocationScopeRequest {

    @NotNull(message = "locationId is required")
    private Long locationId;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    @Schema(description = "Whether this assignment should inherit normal permissions or act with a custom (typically lower) permission set", example = "INHERIT")
    private TemporaryAssignmentMode mode = TemporaryAssignmentMode.INHERIT;

    private String reason;
}
