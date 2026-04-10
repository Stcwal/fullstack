package backend.fullstack.checklist.api.dto;

import java.util.List;

import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistModuleType;

public record ChecklistTemplateResponse(
        Long id,
        String title,
        ChecklistFrequency frequency,
        ChecklistModuleType moduleType,
        List<ChecklistTemplateItemResponse> items
) {
}
