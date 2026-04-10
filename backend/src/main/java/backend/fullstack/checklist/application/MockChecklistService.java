package backend.fullstack.checklist.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.checklist.api.dto.ChecklistInstanceItemResponse;
import backend.fullstack.checklist.api.dto.ChecklistInstanceItemUpdateRequest;
import backend.fullstack.checklist.api.dto.ChecklistInstanceResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateItemResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateUpsertRequest;
import backend.fullstack.checklist.api.dto.CompletedByResponse;
import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceItem;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.checklist.domain.ChecklistTemplate;
import backend.fullstack.checklist.domain.ChecklistTemplateItem;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.checklist.infrastructure.ChecklistTemplateRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;

@Service
@Transactional
public class MockChecklistService implements ChecklistService {

    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistInstanceRepository instanceRepository;

    public MockChecklistService(
            ChecklistTemplateRepository templateRepository,
            ChecklistInstanceRepository instanceRepository
    ) {
        this.templateRepository = templateRepository;
        this.instanceRepository = instanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistTemplateResponse> listTemplates(Long organizationId) {
        return templateRepository.findAllByOrganizationId(organizationId).stream()
                .sorted(Comparator.comparing(ChecklistTemplate::getId))
                .map(this::toTemplateResponse)
                .toList();
    }

    @Override
    public ChecklistTemplateResponse createTemplate(Long organizationId, ChecklistTemplateUpsertRequest request) {
        ChecklistTemplate template = buildTemplate(null, organizationId, request);
        ChecklistTemplate savedTemplate = templateRepository.save(template);
        createMockInstanceForToday(savedTemplate);
        return toTemplateResponse(savedTemplate);
    }

    @Override
    public ChecklistTemplateResponse updateTemplate(Long organizationId, Long templateId, ChecklistTemplateUpsertRequest request) {
        ChecklistTemplate existing = templateRepository.findByIdAndOrganizationId(templateId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist template", templateId));

        existing.setTitle(request.title());
        existing.setFrequency(request.frequency());
        existing.setItems(buildTemplateItems(request.itemTexts()));

        return toTemplateResponse(templateRepository.save(existing));
    }

    @Override
    public void deleteTemplate(Long organizationId, Long templateId) {
        ChecklistTemplate existing = templateRepository.findByIdAndOrganizationId(templateId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist template", templateId));
        templateRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChecklistInstanceResponse> listInstances(
            Long organizationId,
            ChecklistFrequency frequency,
            LocalDate date,
            ChecklistInstanceStatus status,
            Long locationId
    ) {
        return instanceRepository.findAllByOrganizationIdAndOptionalLocation(organizationId, locationId).stream()
                .filter(instance -> frequency == null || instance.getFrequency() == frequency)
                .filter(instance -> date == null || date.equals(instance.getDate()))
                .filter(instance -> status == null || instance.getStatus() == status)
                .sorted(Comparator.comparing(ChecklistInstance::getId))
                .map(this::toInstanceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChecklistInstanceResponse getInstance(Long organizationId, Long instanceId) {
        ChecklistInstance instance = instanceRepository.findByIdAndOrganizationId(instanceId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist instance", instanceId));
        return toInstanceResponse(instance);
    }

    @Override
    public ChecklistInstanceResponse updateInstanceItem(
            Long organizationId,
            Long instanceId,
            Long itemId,
            Long completedByUserId,
            ChecklistInstanceItemUpdateRequest request
    ) {
        ChecklistInstance instance = instanceRepository.findByIdAndOrganizationId(instanceId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist instance", instanceId));

        ChecklistInstanceItem item = instance.getItems().stream()
                .filter(i -> Objects.equals(i.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Checklist instance item", itemId));

        if (Boolean.TRUE.equals(request.completed())) {
            item.setCompleted(true);
            item.setCompletedByUserId(completedByUserId);
            item.setCompletedAt(Instant.now());
        } else {
            item.setCompleted(false);
            item.setCompletedByUserId(null);
            item.setCompletedAt(null);
        }

        instance.setStatus(computeStatus(instance));
        ChecklistInstance saved = instanceRepository.save(instance);
        return toInstanceResponse(saved);
    }

    private ChecklistTemplate buildTemplate(Long templateId, Long organizationId, ChecklistTemplateUpsertRequest request) {
        ChecklistTemplate template = new ChecklistTemplate();
        template.setId(templateId);
        template.setOrganizationId(organizationId);
        template.setTitle(request.title());
        template.setFrequency(request.frequency());
        template.setItems(buildTemplateItems(request.itemTexts()));
        return template;
    }

    private List<ChecklistTemplateItem> buildTemplateItems(List<String> texts) {
        java.util.ArrayList<ChecklistTemplateItem> items = new java.util.ArrayList<>();
        for (String text : texts) {
            items.add(new ChecklistTemplateItem(text));
        }
        return items;
    }

    private void createMockInstanceForToday(ChecklistTemplate template) {
        ChecklistInstance instance = new ChecklistInstance();
        instance.setTemplateId(template.getId());
        instance.setOrganizationId(template.getOrganizationId());
        instance.setTitle(template.getTitle());
        instance.setFrequency(template.getFrequency());
        instance.setDate(LocalDate.now());
        instance.setStatus(ChecklistInstanceStatus.PENDING);

        List<ChecklistInstanceItem> items = template.getItems().stream().map(templateItem -> {
            ChecklistInstanceItem instanceItem = new ChecklistInstanceItem();
            instanceItem.setText(templateItem.getText());
            instanceItem.setCompleted(false);
            return instanceItem;
        }).toList();

        instance.setItems(items);
        instanceRepository.save(instance);
    }

    private ChecklistInstanceStatus computeStatus(ChecklistInstance instance) {
        long completedCount = instance.getItems().stream().filter(ChecklistInstanceItem::isCompleted).count();
        if (completedCount == 0) {
            return ChecklistInstanceStatus.PENDING;
        }
        if (completedCount == instance.getItems().size()) {
            return ChecklistInstanceStatus.COMPLETED;
        }
        return ChecklistInstanceStatus.IN_PROGRESS;
    }

    private ChecklistTemplateResponse toTemplateResponse(ChecklistTemplate template) {
        return new ChecklistTemplateResponse(
                template.getId(),
                template.getTitle(),
                template.getFrequency(),
                template.getItems().stream()
                        .map(item -> new ChecklistTemplateItemResponse(item.getId(), item.getText()))
                        .toList()
        );
    }

    private ChecklistInstanceResponse toInstanceResponse(ChecklistInstance instance) {
        int totalCount = instance.getItems().size();
        int completedCount = (int) instance.getItems().stream().filter(ChecklistInstanceItem::isCompleted).count();

        List<ChecklistInstanceItemResponse> itemResponses = instance.getItems().stream()
                .map(item -> {
                    CompletedByResponse completedBy = item.getCompletedByUserId() == null
                            ? null
                            : new CompletedByResponse(item.getCompletedByUserId(), "Mock User " + item.getCompletedByUserId());
                    return new ChecklistInstanceItemResponse(
                            item.getId(),
                            item.getText(),
                            item.isCompleted(),
                            completedBy,
                            item.getCompletedAt()
                    );
                })
                .toList();

        return new ChecklistInstanceResponse(
                instance.getId(),
                instance.getTemplateId(),
                instance.getTitle(),
                instance.getFrequency(),
                instance.getDate(),
                completedCount,
                totalCount,
                instance.getStatus(),
                itemResponses
        );
    }
}
