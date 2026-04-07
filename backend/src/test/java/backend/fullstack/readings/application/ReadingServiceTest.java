package backend.fullstack.readings.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.readings.api.dto.ReadingMapper;
import backend.fullstack.readings.api.dto.ReadingRequest;
import backend.fullstack.readings.api.dto.ReadingResponse;
import backend.fullstack.readings.domain.TemperatureReading;
import backend.fullstack.readings.infrastructure.TemperatureReadingRepository;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.domain.UnitType;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private TemperatureReadingRepository readingRepository;

    @Mock
    private TemperatureUnitRepository unitRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadingMapper readingMapper;

    private ReadingService service;

    @BeforeEach
    void setUp() {
        service = new ReadingService(readingRepository, unitRepository, organizationRepository, userRepository, readingMapper);
    }

    @Test
    void getReadingsForUnit_returnsReadingsForValidUnit() {
        Long orgId = 1L;
        Long unitId = 10L;

        TemperatureUnit unit = buildUnit(unitId, orgId, 0.0, 8.0);
        TemperatureReading reading1 = buildReading(1L, unit);
        TemperatureReading reading2 = buildReading(2L, unit);

        when(unitRepository.findByIdAndOrganization_Id(unitId, orgId)).thenReturn(Optional.of(unit));
        when(readingRepository.findByUnit_IdAndOrganization_IdOrderByRecordedAtDesc(unitId, orgId))
                .thenReturn(List.of(reading1, reading2));
        when(readingMapper.toResponse(reading1)).thenReturn(buildResponse(1L, unitId, 3.0, false));
        when(readingMapper.toResponse(reading2)).thenReturn(buildResponse(2L, unitId, 5.0, false));

        List<ReadingResponse> result = service.getReadingsForUnit(orgId, unitId);

        assertEquals(2, result.size());
    }

    @Test
    void recordReading_calculatesOutOfRange_whenTempExceedsMax() {
        Long orgId = 1L;
        Long userId = 5L;
        Long unitId = 10L;

        TemperatureUnit unit = buildUnit(unitId, orgId, 0.0, 8.0);
        User user = buildUser(userId, orgId);
        Organization organization = buildOrganization(orgId);

        when(unitRepository.findByIdAndOrganization_Id(unitId, orgId)).thenReturn(Optional.of(unit));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(readingRepository.save(any(TemperatureReading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(readingMapper.toResponse(any(TemperatureReading.class)))
                .thenAnswer(invocation -> {
                    TemperatureReading r = invocation.getArgument(0);
                    return buildResponse(1L, unitId, r.getTemperature(), r.isOutOfRange());
                });

        ReadingRequest request = new ReadingRequest(10.0, LocalDateTime.now(), null);
        ReadingResponse response = service.recordReading(orgId, userId, unitId, request);

        assertTrue(response.isOutOfRange(), "Temperature 10.0 exceeds max 8.0 — should be out of range");

        ArgumentCaptor<TemperatureReading> captor = ArgumentCaptor.forClass(TemperatureReading.class);
        verify(readingRepository).save(captor.capture());
        assertTrue(captor.getValue().isOutOfRange());
    }

    @Test
    void recordReading_calculatesInRange_whenTempWithinThresholds() {
        Long orgId = 1L;
        Long userId = 5L;
        Long unitId = 10L;

        TemperatureUnit unit = buildUnit(unitId, orgId, 0.0, 8.0);
        User user = buildUser(userId, orgId);
        Organization organization = buildOrganization(orgId);

        when(unitRepository.findByIdAndOrganization_Id(unitId, orgId)).thenReturn(Optional.of(unit));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
        when(readingRepository.save(any(TemperatureReading.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(readingMapper.toResponse(any(TemperatureReading.class)))
                .thenAnswer(invocation -> {
                    TemperatureReading r = invocation.getArgument(0);
                    return buildResponse(1L, unitId, r.getTemperature(), r.isOutOfRange());
                });

        ReadingRequest request = new ReadingRequest(3.0, LocalDateTime.now(), null);
        ReadingResponse response = service.recordReading(orgId, userId, unitId, request);

        assertFalse(response.isOutOfRange(), "Temperature 3.0 is within [0.0, 8.0] — should be in range");

        ArgumentCaptor<TemperatureReading> captor = ArgumentCaptor.forClass(TemperatureReading.class);
        verify(readingRepository).save(captor.capture());
        assertFalse(captor.getValue().isOutOfRange());
    }

    @Test
    void getReadingsForUnit_throwsUnitNotFoundException_whenUnitNotInOrg() {
        Long orgId = 1L;
        Long unitId = 99L;

        when(unitRepository.findByIdAndOrganization_Id(unitId, orgId)).thenReturn(Optional.empty());

        assertThrows(UnitNotFoundException.class, () -> service.getReadingsForUnit(orgId, unitId));
    }

    // --- helpers ---

    private static TemperatureUnit buildUnit(Long id, Long orgId, double min, double max) {
        Organization org = buildOrganization(orgId);
        return TemperatureUnit.builder()
                .id(id)
                .organization(org)
                .name("Freezer A")
                .type(UnitType.FREEZER)
                .targetTemperature(4.0)
                .minThreshold(min)
                .maxThreshold(max)
                .active(true)
                .build();
    }

    private static Organization buildOrganization(Long id) {
        return Organization.builder()
                .id(id)
                .name("Everest")
                .organizationNumber("123456789")
                .build();
    }

    private static User buildUser(Long id, Long orgId) {
        return User.builder()
                .id(id)
                .firstName("Per")
                .lastName("Olsen")
                .email("per@everestsushi.no")
                .passwordHash("hashed")
                .organization(buildOrganization(orgId))
                .build();
    }

    private static TemperatureReading buildReading(Long id, TemperatureUnit unit) {
        return TemperatureReading.builder()
                .id(id)
                .unit(unit)
                .temperature(3.0)
                .recordedAt(LocalDateTime.now())
                .isOutOfRange(false)
                .build();
    }

    private static ReadingResponse buildResponse(Long id, Long unitId, double temp, boolean outOfRange) {
        return new ReadingResponse(id, unitId, temp, LocalDateTime.now(), "Per Olsen", null, outOfRange);
    }
}
