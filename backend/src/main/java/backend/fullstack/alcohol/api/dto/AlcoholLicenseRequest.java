package backend.fullstack.alcohol.api.dto;

import java.time.LocalDate;

import backend.fullstack.alcohol.domain.LicenseType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to create or update an alcohol license")
public class AlcoholLicenseRequest {

    @NotNull(message = "License type is required")
    @Schema(description = "Type of alcohol license", example = "FULL_LICENSE")
    private LicenseType licenseType;

    @Schema(description = "License number from the municipality", example = "OSL-2026-1234")
    private String licenseNumber;

    @Schema(description = "Date the license was issued", example = "2026-01-15")
    private LocalDate issuedAt;

    @Schema(description = "Date the license expires", example = "2030-01-15")
    private LocalDate expiresAt;

    @Schema(description = "Authority that issued the license", example = "Oslo kommune")
    private String issuingAuthority;

    @Schema(description = "Additional notes about the license")
    private String notes;
}
