package backend.fullstack.alcohol.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import backend.fullstack.alcohol.domain.LicenseType;
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
@Schema(description = "Alcohol license response")
public class AlcoholLicenseResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "100")
    private Long organizationId;

    @Schema(example = "FULL_LICENSE")
    private LicenseType licenseType;

    @Schema(example = "OSL-2026-1234")
    private String licenseNumber;

    @Schema(example = "2026-01-15")
    private LocalDate issuedAt;

    @Schema(example = "2030-01-15")
    private LocalDate expiresAt;

    @Schema(example = "Oslo kommune")
    private String issuingAuthority;

    private String notes;

    @Schema(description = "Whether the license has expired")
    private boolean expired;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
