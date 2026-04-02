package backend.fullstack.checklist.api.dto;

import java.util.List;

import backend.fullstack.checklist.domain.ChecklistFrequency;

public record ChecklistTemplateResponse(
        Long id,
        String title,
        ChecklistFrequency frequency,
        List<ChecklistTemplateItemResponse> items
) {
}
