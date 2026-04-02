package backend.fullstack.checklist.api.dto;

import java.util.List;

import backend.fullstack.checklist.domain.ChecklistFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChecklistTemplateUpsertRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull ChecklistFrequency frequency,
        @NotEmpty List<@NotBlank @Size(max = 250) String> itemTexts
) {
}
