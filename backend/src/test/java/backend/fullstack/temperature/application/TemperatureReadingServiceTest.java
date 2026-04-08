package backend.fullstack.temperature.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.temperature.api.dto.RecordedByResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingDeviationResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingMapper;
import backend.fullstack.temperature.api.dto.TemperatureReadingRequest;
import backend.fullstack.temperature.api.dto.TemperatureReadingResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsGroupBy;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsResponse;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.domain.UnitType;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

@ExtendWith(MockitoExtension.class)
class TemperatureReadingServiceTest {

    @Mock
    private TemperatureReadingRepository readingRepository;

    @Mock
    private TemperatureUnitRepository unitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemperatureReadingMapper readingMapper;

    private TemperatureReadingService service;

    @BeforeEach
    void setUp() {
        service = new TemperatureReadingService(readingRepository, unitRepository, userRepository, readingMapper);
    }

    @Test
    void createReadingEvaluatesDeviationAndPersistsReading() {
        JwtPrincipal principal = new JwtPrincipal(10L, "staff@everest.no", Role.STAFF, 1L, List.of());
        TemperatureUnit unit = unit(5L, 1L, true, 1.0, 5.0);
        User user = user(10L, 1L);
        TemperatureReadingRequest request = new TemperatureReadingRequest(6.5, null, "Door open");
        TemperatureReading mapped = TemperatureReading.builder()
                .temperature(6.5)
                .recordedAt(LocalDateTime.of(2026, 3, 20, 8, 10))
                .note("Door open")
                .build();
        TemperatureReading saved = TemperatureReading.builder()
                .id(99L)
                .organization(unit.getOrganization())
                .unit(unit)
                .recordedBy(user)
                .temperature(6.5)
                .recordedAt(mapped.getRecordedAt())
                .note("Door open")
                .isDeviation(true)
                .build();
        TemperatureReadingResponse expected = response(99L, true);

        when(unitRepository.findByIdAndOrganization_Id(5L, 1L)).thenReturn(Optional.of(unit));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(readingMapper.toEntity(request)).thenReturn(mapped);
        when(readingRepository.save(any(TemperatureReading.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(readingMapper.toResponse(any(TemperatureReading.class))).thenReturn(expected);

        TemperatureReadingResponse actual = service.createReading(principal, 5L, request);

        ArgumentCaptor<TemperatureReading> captor = ArgumentCaptor.forClass(TemperatureReading.class);
        verify(readingRepository).save(captor.capture());

        TemperatureReading persisted = captor.getValue();
        assertEquals(1L, persisted.getOrganizationId());
        assertEquals(5L, persisted.getUnitId());
        assertEquals(10L, persisted.getRecordedByUserId());
        assertTrue(persisted.isDeviation());
        assertNotNull(persisted.getRecordedAt());
        assertEquals(expected, actual);
    }

    @Test
    void createReadingThrowsWhenUnitIsInactive() {
        JwtPrincipal principal = new JwtPrincipal(10L, "staff@everest.no", Role.STAFF, 1L, List.of());
        TemperatureUnit unit = unit(5L, 1L, false, 1.0, 5.0);

        when(unitRepository.findByIdAndOrganization_Id(5L, 1L)).thenReturn(Optional.of(unit));

        assertThrows(UnitInactiveException.class, () -> service.createReading(
                principal,
                5L,
                new TemperatureReadingRequest(2.0, null, null)
        ));

        verify(userRepository, never()).findById(any());
        verify(readingRepository, never()).save(any());
    }

    @Test
    void createReadingThrowsWhenUnitIsMissing() {
        JwtPrincipal principal = new JwtPrincipal(10L, "staff@everest.no", Role.STAFF, 1L, List.of());

        when(unitRepository.findByIdAndOrganization_Id(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(UnitNotFoundException.class, () -> service.createReading(
                principal,
                5L,
                new TemperatureReadingRequest(2.0, null, null)
        ));

        verify(userRepository, never()).findById(any());
        verify(readingRepository, never()).save(any());
    }

    @Test
    void listReadingsReturnsPagedMappedResults() {
        TemperatureReading reading = reading(1L, true);
        TemperatureReadingResponse response = response(1L, true);

        when(readingRepository.findAllByOrganizationWithFilters(1L, 5L, null, null, true, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(reading)));
        when(readingMapper.toResponse(reading)).thenReturn(response);

        var page = service.listReadings(1L, 5L, null, null, true, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements());
        assertEquals(response, page.getContent().get(0));
    }

    @Test
    void getReadingThrowsWhenReadingMissingForOrganization() {
        when(readingRepository.findByIdAndOrganization_Id(11L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getReading(1L, 11L));
    }

    @Test
    void listUnitReadingsMapsResults() {
        TemperatureReading reading = reading(1L, false);
        TemperatureReadingResponse response = response(1L, false);

        when(readingRepository.findByOrganizationAndUnitAndRecordedAtBetween(1L, 5L, null, null))
                .thenReturn(List.of(reading));
        when(readingMapper.toResponse(reading)).thenReturn(response);

        List<TemperatureReadingResponse> results = service.listUnitReadings(1L, 5L, null, null);

        assertEquals(1, results.size());
        assertEquals(response, results.get(0));
        assertFalse(results.get(0).isDeviation());
    }

    @Test
    void getReadingStatsBuildsSeriesAndDeviationList() {
        TemperatureReading freezerMorning = readingAt(11L, 5L, "Freezer A", 2.0, false, LocalDateTime.of(2026, 3, 20, 8, 10));
        TemperatureReading freezerEveningDeviation = readingAt(12L, 5L, "Freezer A", 6.0, true, LocalDateTime.of(2026, 3, 20, 20, 15));
        TemperatureReading fridgeDayTwo = readingAt(13L, 9L, "Fridge B", 3.0, false, LocalDateTime.of(2026, 3, 21, 9, 30));

        when(readingRepository.findForStatsByOrganizationAndRange(1L, null, null))
                .thenReturn(List.of(freezerMorning, freezerEveningDeviation, fridgeDayTwo));

        TemperatureReadingStatsResponse stats = service.getReadingStats(
                1L,
                null,
                null,
                null,
                TemperatureReadingStatsGroupBy.DAY
        );

        assertEquals(2, stats.series().size());

        var freezerSeries = stats.series().stream()
                .filter(series -> series.unitId().equals(5L))
                .findFirst()
                .orElseThrow();
        assertEquals(1, freezerSeries.dataPoints().size());
        assertTrue(freezerSeries.dataPoints().get(0).isDeviation());
        assertEquals(4.0, freezerSeries.dataPoints().get(0).avgTemperature());

        assertEquals(1, stats.deviations().size());
        TemperatureReadingDeviationResponse deviation = stats.deviations().get(0);
        assertEquals(12L, deviation.id());
        assertEquals(5.0, deviation.threshold());
    }

    @Test
    void getReadingStatsUsesUnitFilterWhenUnitIdsProvided() {
        TemperatureReading freezerDeviation = readingAt(22L, 5L, "Freezer A", 6.0, true, LocalDateTime.of(2026, 3, 20, 8, 10));

        when(readingRepository.findForStatsByOrganizationAndUnitIdsAndRange(1L, List.of(5L), null, null))
                .thenReturn(List.of(freezerDeviation));

        TemperatureReadingStatsResponse stats = service.getReadingStats(
                1L,
                List.of(5L, 5L),
                null,
                null,
                TemperatureReadingStatsGroupBy.HOUR
        );

        verify(readingRepository).findForStatsByOrganizationAndUnitIdsAndRange(1L, List.of(5L), null, null);
        assertEquals(1, stats.series().size());
    }

    private static TemperatureUnit unit(Long id, Long organizationId, boolean active, double min, double max) {
        Organization organization = Organization.builder()
                .id(organizationId)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        return TemperatureUnit.builder()
                .id(id)
                .organization(organization)
                .name("Freezer A")
                .type(UnitType.FREEZER)
                .targetTemperature(3.0)
                .minThreshold(min)
                .maxThreshold(max)
                .description("Main storage")
                .active(active)
                .build();
    }

    private static User user(Long id, Long organizationId) {
        Organization organization = Organization.builder()
                .id(organizationId)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        return User.builder()
                .id(id)
                .organization(organization)
                .email("staff@everest.no")
                .firstName("Kari")
                .lastName("Larsen")
                .passwordHash("hash")
                .role(Role.STAFF)
                .build();
    }

    private static TemperatureReading reading(Long id, boolean deviation) {
        Organization organization = Organization.builder()
                .id(1L)
                .name("Everest")
                .organizationNumber("123456789")
                .build();
        TemperatureUnit unit = unit(5L, 1L, true, 1.0, 5.0);
        User user = user(10L, 1L);

        return TemperatureReading.builder()
                .id(id)
                .organization(organization)
                .unit(unit)
                .recordedBy(user)
                .temperature(2.5)
                .recordedAt(LocalDateTime.of(2026, 3, 20, 8, 10))
                .note("OK")
                .isDeviation(deviation)
                .build();
    }

    private static TemperatureReading readingAt(
            Long id,
            Long unitId,
            String unitName,
            Double temperature,
            boolean deviation,
            LocalDateTime recordedAt
    ) {
        Organization organization = Organization.builder()
                .id(1L)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        TemperatureUnit unit = TemperatureUnit.builder()
                .id(unitId)
                .organization(organization)
                .name(unitName)
                .type(UnitType.FREEZER)
                .targetTemperature(3.0)
                .minThreshold(1.0)
                .maxThreshold(5.0)
                .active(true)
                .build();

        return TemperatureReading.builder()
                .id(id)
                .organization(organization)
                .unit(unit)
                .recordedBy(user(10L, 1L))
                .temperature(temperature)
                .recordedAt(recordedAt)
                .isDeviation(deviation)
                .build();
    }

    private static TemperatureReadingResponse response(Long id, boolean deviation) {
        return new TemperatureReadingResponse(
                id,
                1L,
                5L,
                "Freezer A",
                2.5,
                3.0,
                1.0,
                5.0,
                deviation,
                LocalDateTime.of(2026, 3, 20, 8, 10),
                "OK",
                new RecordedByResponse(10L, "Kari Larsen"),
                null,
                null
        );
    }
}