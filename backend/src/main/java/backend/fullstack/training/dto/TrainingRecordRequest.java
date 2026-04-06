package backend.fullstack.training.dto;

import java.time.LocalDateTime;

import backend.fullstack.training.TrainingStatus;
import backend.fullstack.training.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for creating or updating a training record.
 */
@Getter
@Setter
@Schema(description = "Training record request")
public class TrainingRecordRequest {

    @NotNull(message = "User id is required")
    @Schema(description = "User id the training belongs to", example = "12")
    private Long userId;

    @NotNull(message = "Training type is required")
    @Schema(description = "Training type", example = "GENERAL")
    private TrainingType trainingType;

    @NotNull(message = "Training status is required")
    @Schema(description = "Training status", example = "COMPLETED")
    private TrainingStatus status;

    @Schema(description = "When the training was completed")
    private LocalDateTime completedAt;

    @Schema(description = "When the training expires")
    private LocalDateTime expiresAt;
}
