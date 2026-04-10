package backend.fullstack.checklist.api.dto;

import java.time.LocalDate;
import java.util.List;

import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.checklist.domain.ChecklistModuleType;

public record ChecklistInstanceResponse(
        Long id,
        Long templateId,
        String title,
        ChecklistFrequency frequency,
        ChecklistModuleType moduleType,
        LocalDate date,
        int completedCount,
        int totalCount,
        ChecklistInstanceStatus status,
        List<ChecklistInstanceItemResponse> items
) {
}
