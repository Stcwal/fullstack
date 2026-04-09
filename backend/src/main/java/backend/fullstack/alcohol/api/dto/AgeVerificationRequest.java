package backend.fullstack.alcohol.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.alcohol.domain.VerificationMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to log an age verification check")
public class AgeVerificationRequest {

    @NotNull(message = "Location id is required")
    @Schema(description = "Location where verification was performed", example = "1")
    private Long locationId;

    @NotNull(message = "Verification method is required")
    @Schema(description = "How the age was verified", example = "ID_CHECKED")
    private VerificationMethod verificationMethod;

    @Schema(description = "Whether the guest appeared to be underage", example = "true")
    private boolean guestAppearedUnderage = true;

    @Schema(description = "Whether the ID was valid (null if no ID checked)")
    private Boolean idWasValid;

    @Schema(description = "Whether service was refused", example = "false")
    private boolean wasRefused = false;

    @Schema(description = "Additional notes about the verification")
    private String note;

    @Schema(description = "When the verification occurred (defaults to now)")
    private LocalDateTime verifiedAt;
}
