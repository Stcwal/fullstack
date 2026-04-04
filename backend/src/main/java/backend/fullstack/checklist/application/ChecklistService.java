package backend.fullstack.checklist.application;

import java.time.LocalDate;
import java.util.List;

import backend.fullstack.checklist.api.dto.ChecklistInstanceItemUpdateRequest;
import backend.fullstack.checklist.api.dto.ChecklistInstanceResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateUpsertRequest;
import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;

public interface ChecklistService {

    List<ChecklistTemplateResponse> listTemplates(Long organizationId);

    ChecklistTemplateResponse createTemplate(Long organizationId, ChecklistTemplateUpsertRequest request);

    ChecklistTemplateResponse updateTemplate(Long organizationId, Long templateId, ChecklistTemplateUpsertRequest request);

    void deleteTemplate(Long organizationId, Long templateId);

    List<ChecklistInstanceResponse> listInstances(
            Long organizationId,
            ChecklistFrequency frequency,
            LocalDate date,
            ChecklistInstanceStatus status
    );

    ChecklistInstanceResponse getInstance(Long organizationId, Long instanceId);

    ChecklistInstanceResponse updateInstanceItem(
            Long organizationId,
            Long instanceId,
            Long itemId,
            Long completedByUserId,
            ChecklistInstanceItemUpdateRequest request
    );
}
