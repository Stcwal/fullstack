package backend.fullstack.checklist.application;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.fullstack.checklist.api.dto.ChecklistInstanceItemUpdateRequest;
import backend.fullstack.checklist.api.dto.ChecklistInstanceResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateResponse;
import backend.fullstack.checklist.api.dto.ChecklistTemplateUpsertRequest;
import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.checklist.infrastructure.ChecklistTemplateRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;

class MockChecklistServiceTest {

    private MockChecklistService service;

    @BeforeEach
    void setUp() {
        service = new MockChecklistService(
                new ChecklistTemplateRepository(),
                new ChecklistInstanceRepository()
        );
    }

    @Test
    void createTemplateCreatesTemplateAndMockInstanceForSameOrganization() {
        ChecklistTemplateUpsertRequest request = new ChecklistTemplateUpsertRequest(
                "Opening kitchen",
                ChecklistFrequency.DAILY,
                List.of("Sanitize worktops", "Check freezer")
        );

        ChecklistTemplateResponse created = service.createTemplate(1L, request);

        assertNotNull(created.id());
        assertEquals("Opening kitchen", created.title());
        assertEquals(2, created.items().size());

        List<ChecklistTemplateResponse> orgOneTemplates = service.listTemplates(1L);
        List<ChecklistTemplateResponse> orgTwoTemplates = service.listTemplates(2L);
        assertEquals(1, orgOneTemplates.size());
        assertTrue(orgTwoTemplates.isEmpty());

        List<ChecklistInstanceResponse> instances = service.listInstances(1L, null, null, null);
        assertEquals(1, instances.size());
        assertEquals(ChecklistInstanceStatus.PENDING, instances.get(0).status());
        assertEquals(LocalDate.now(), instances.get(0).date());
    }

    @Test
    void updateInstanceItemTransitionsStatusAndTracksCompletedBy() {
        ChecklistTemplateResponse template = service.createTemplate(
                1L,
                new ChecklistTemplateUpsertRequest(
                        "Weekly close",
                        ChecklistFrequency.WEEKLY,
                        List.of("Lock storage", "Turn off lights")
                )
        );

        ChecklistInstanceResponse instance = service.listInstances(1L, null, null, null).get(0);
        Long instanceId = instance.id();

        ChecklistInstanceResponse afterFirstComplete = service.updateInstanceItem(
                1L,
                instanceId,
                template.items().get(0).id(),
                44L,
                new ChecklistInstanceItemUpdateRequest(true)
        );

        assertEquals(ChecklistInstanceStatus.IN_PROGRESS, afterFirstComplete.status());
        assertEquals(1, afterFirstComplete.completedCount());
        assertEquals(44L, afterFirstComplete.items().get(0).completedBy().id());
        assertNotNull(afterFirstComplete.items().get(0).completedAt());

        ChecklistInstanceResponse afterSecondComplete = service.updateInstanceItem(
                1L,
                instanceId,
                template.items().get(1).id(),
                44L,
                new ChecklistInstanceItemUpdateRequest(true)
        );

        assertEquals(ChecklistInstanceStatus.COMPLETED, afterSecondComplete.status());
        assertEquals(2, afterSecondComplete.completedCount());

        ChecklistInstanceResponse afterUncheck = service.updateInstanceItem(
                1L,
                instanceId,
                template.items().get(0).id(),
                44L,
                new ChecklistInstanceItemUpdateRequest(false)
        );

        assertEquals(ChecklistInstanceStatus.IN_PROGRESS, afterUncheck.status());
        assertEquals(1, afterUncheck.completedCount());
        assertNull(afterUncheck.items().get(0).completedBy());
        assertNull(afterUncheck.items().get(0).completedAt());
    }

    @Test
    void listInstancesAppliesFrequencyDateAndStatusFilters() {
        service.createTemplate(
                1L,
                new ChecklistTemplateUpsertRequest("Daily check", ChecklistFrequency.DAILY, List.of("A"))
        );
        service.createTemplate(
                1L,
                new ChecklistTemplateUpsertRequest("Weekly check", ChecklistFrequency.WEEKLY, List.of("B"))
        );

        List<ChecklistInstanceResponse> dailyInstances = service.listInstances(
                1L,
                ChecklistFrequency.DAILY,
                null,
                null
        );
        assertEquals(1, dailyInstances.size());

        ChecklistInstanceResponse weekly = service.listInstances(
                1L,
                ChecklistFrequency.WEEKLY,
                null,
                null
        ).get(0);

        service.updateInstanceItem(
                1L,
                weekly.id(),
                weekly.items().get(0).id(),
                9L,
                new ChecklistInstanceItemUpdateRequest(true)
        );

        List<ChecklistInstanceResponse> completedWeekly = service.listInstances(
                1L,
                ChecklistFrequency.WEEKLY,
                LocalDate.now(),
                ChecklistInstanceStatus.COMPLETED
        );

        assertEquals(1, completedWeekly.size());
        assertEquals(ChecklistFrequency.WEEKLY, completedWeekly.get(0).frequency());
        assertEquals(ChecklistInstanceStatus.COMPLETED, completedWeekly.get(0).status());
    }

    @Test
    void deleteTemplateThrowsWhenTemplateDoesNotExistInOrganization() {
        assertThrows(ResourceNotFoundException.class, () -> service.deleteTemplate(1L, 999L));
    }
}
