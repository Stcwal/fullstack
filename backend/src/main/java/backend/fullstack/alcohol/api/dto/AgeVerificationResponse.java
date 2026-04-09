package backend.fullstack.alcohol.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.alcohol.domain.VerificationMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Age verification log response")
public class AgeVerificationResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "100")
    private Long organizationId;

    @Schema(example = "1")
    private Long locationId;

    @Schema(example = "Bar")
    private String locationName;

    @Schema(example = "5")
    private Long verifiedByUserId;

    @Schema(example = "Ola Nordmann")
    private String verifiedByName;

    @Schema(example = "ID_CHECKED")
    private VerificationMethod verificationMethod;

    private boolean guestAppearedUnderage;

    private Boolean idWasValid;

    private boolean wasRefused;

    private String note;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
}
