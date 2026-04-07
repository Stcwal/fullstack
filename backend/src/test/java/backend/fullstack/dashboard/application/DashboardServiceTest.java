package backend.fullstack.dashboard.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.checklist.domain.ChecklistFrequency;
import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.dashboard.api.dto.DashboardResponse;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.readings.infrastructure.TemperatureReadingRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TemperatureReadingRepository readingRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private ChecklistInstanceRepository checklistInstanceRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(readingRepository, deviationRepository, checklistInstanceRepository);
    }

    @Test
    void getDashboard_returnsStatsWithOpenDeviationCount() {
        Long orgId = 1L;

        when(readingRepository.countByOrganization_IdAndIsOutOfRangeTrueAndRecordedAtAfter(
                org.mockito.ArgumentMatchers.eq(orgId),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);

        when(deviationRepository.countByOrganization_IdAndStatus(orgId, DeviationStatus.OPEN))
                .thenReturn(3L);

        when(checklistInstanceRepository.findAllByOrganizationId(orgId))
                .thenReturn(List.of());

        when(deviationRepository.findByOrganization_IdAndStatusOrderByCreatedAtDesc(orgId, DeviationStatus.OPEN))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(orgId);

        assertEquals(3L, response.stats().openDeviations());
    }

    @Test
    void getDashboard_tasksListEmptyWhenNoInstances() {
        Long orgId = 2L;

        when(readingRepository.countByOrganization_IdAndIsOutOfRangeTrueAndRecordedAtAfter(
                org.mockito.ArgumentMatchers.eq(orgId),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);

        when(deviationRepository.countByOrganization_IdAndStatus(orgId, DeviationStatus.OPEN))
                .thenReturn(0L);

        when(checklistInstanceRepository.findAllByOrganizationId(orgId))
                .thenReturn(List.of());

        when(deviationRepository.findByOrganization_IdAndStatusOrderByCreatedAtDesc(orgId, DeviationStatus.OPEN))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(orgId);

        assertTrue(response.tasks().isEmpty());
    }

    @Test
    void getDashboard_onlyTodayInstancesIncludedAsTasks() {
        Long orgId = 3L;

        // Instance for today — should be included
        ChecklistInstance todayInstance = buildInstance(1L, orgId, LocalDate.now(), ChecklistInstanceStatus.PENDING);
        // Instance for yesterday — should be excluded
        ChecklistInstance yesterdayInstance = buildInstance(2L, orgId, LocalDate.now().minusDays(1), ChecklistInstanceStatus.PENDING);

        when(readingRepository.countByOrganization_IdAndIsOutOfRangeTrueAndRecordedAtAfter(
                org.mockito.ArgumentMatchers.eq(orgId),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);

        when(deviationRepository.countByOrganization_IdAndStatus(orgId, DeviationStatus.OPEN))
                .thenReturn(0L);

        when(checklistInstanceRepository.findAllByOrganizationId(orgId))
                .thenReturn(List.of(todayInstance, yesterdayInstance));

        when(deviationRepository.findByOrganization_IdAndStatusOrderByCreatedAtDesc(orgId, DeviationStatus.OPEN))
                .thenReturn(List.of());

        DashboardResponse response = service.getDashboard(orgId);

        assertEquals(1, response.tasks().size());
        assertEquals(1L, response.tasks().get(0).id());
    }

    // --- helpers ---

    private static ChecklistInstance buildInstance(
            Long id, Long orgId, LocalDate date, ChecklistInstanceStatus status
    ) {
        ChecklistInstance instance = new ChecklistInstance();
        instance.setId(id);
        instance.setOrganizationId(orgId);
        instance.setTemplateId(100L);
        instance.setTitle("Daglig rengjøring");
        instance.setFrequency(ChecklistFrequency.DAILY);
        instance.setDate(date);
        instance.setStatus(status);
        return instance;
    }
}
