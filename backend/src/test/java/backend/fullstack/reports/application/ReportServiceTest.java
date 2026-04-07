package backend.fullstack.reports.application;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.organization.Organization;
import backend.fullstack.readings.infrastructure.TemperatureReadingRepository;
import backend.fullstack.reports.api.dto.ChartResponse;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.domain.UnitType;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TemperatureUnitRepository unitRepository;

    @Mock
    private TemperatureReadingRepository readingRepository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(unitRepository, readingRepository);
    }

    @Test
    void getChartData_returnsWeekLabels() {
        Long orgId = 1L;
        when(unitRepository.findByOrganizationAndOptionalActive(orgId, true)).thenReturn(List.of());

        ChartResponse response = service.getChartData(orgId, "WEEK");

        assertEquals(7, response.labels().size());
    }

    @Test
    void getChartData_returnsMonthLabels() {
        Long orgId = 1L;
        when(unitRepository.findByOrganizationAndOptionalActive(orgId, true)).thenReturn(List.of());

        ChartResponse response = service.getChartData(orgId, "MONTH");

        assertEquals(30, response.labels().size());
    }

    @Test
    void getChartData_datasetsMatchActiveUnits() {
        Long orgId = 1L;
        TemperatureUnit unit1 = buildUnit(1L, orgId, "Frys 1");
        TemperatureUnit unit2 = buildUnit(2L, orgId, "Kjøleskap 1");

        when(unitRepository.findByOrganizationAndOptionalActive(orgId, true))
                .thenReturn(List.of(unit1, unit2));

        when(readingRepository.findByUnit_IdAndOrganization_IdOrderByRecordedAtDesc(1L, orgId))
                .thenReturn(List.of());
        when(readingRepository.findByUnit_IdAndOrganization_IdOrderByRecordedAtDesc(2L, orgId))
                .thenReturn(List.of());

        ChartResponse response = service.getChartData(orgId, "WEEK");

        assertEquals(2, response.datasets().size());
        assertEquals("Frys 1", response.datasets().get(0).label());
        assertEquals("Kjøleskap 1", response.datasets().get(1).label());
    }

    @Test
    void getChartData_defaultsToWeekWhenUnknownPeriod() {
        Long orgId = 1L;
        when(unitRepository.findByOrganizationAndOptionalActive(orgId, true)).thenReturn(List.of());

        ChartResponse response = service.getChartData(orgId, "UNKNOWN");

        assertEquals(7, response.labels().size());
    }

    // --- helpers ---

    private static TemperatureUnit buildUnit(Long id, Long orgId, String name) {
        Organization org = Organization.builder()
                .id(orgId)
                .name("Everest")
                .organizationNumber("937219997")
                .build();

        return TemperatureUnit.builder()
                .id(id)
                .organization(org)
                .name(name)
                .type(UnitType.FREEZER)
                .targetTemperature(-18.0)
                .minThreshold(-22.0)
                .maxThreshold(-15.0)
                .active(true)
                .build();
    }
}
