package backend.fullstack.training.dto;

import java.time.LocalDateTime;

import backend.fullstack.training.TrainingStatus;
import backend.fullstack.training.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response payload for training records.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Training record response")
public class TrainingRecordResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "12")
    private Long userId;

    @Schema(example = "staff@everest.no")
    private String userEmail;

    @Schema(example = "Ola Nordmann")
    private String userName;

    @Schema(example = "100")
    private Long organizationId;

    @Schema(example = "GENERAL")
    private TrainingType trainingType;

    @Schema(example = "COMPLETED")
    private TrainingStatus status;

    @Schema(description = "When the training was completed")
    private LocalDateTime completedAt;

    @Schema(description = "When the training expires")
    private LocalDateTime expiresAt;
}
