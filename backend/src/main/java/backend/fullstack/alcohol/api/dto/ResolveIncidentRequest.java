package backend.fullstack.alcohol.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to resolve an alcohol serving incident")
public class ResolveIncidentRequest {

    @NotBlank(message = "Corrective action is required")
    @Schema(description = "Corrective action taken to resolve the incident",
            example = "Staff member received additional training on responsible serving")
    private String correctiveAction;
}
