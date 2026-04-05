package backend.fullstack.units.application;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import backend.fullstack.exceptions.InvalidThresholdException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.units.api.dto.UnitMapper;
import backend.fullstack.units.api.dto.UnitRequest;
import backend.fullstack.units.api.dto.UnitResponse;
import backend.fullstack.units.api.dto.UnitStatusRequest;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.domain.UnitType;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private TemperatureUnitRepository unitRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UnitMapper unitMapper;

    private UnitService service;

    @BeforeEach
    void setUp() {
        service = new UnitService(unitRepository, organizationRepository, unitMapper);
    }

    @Test
    void createUnitThrowsWhenThresholdsAreInvalid() {
        UnitRequest request = request(5.0, 3.0, 8.0);

        assertThrows(InvalidThresholdException.class, () -> service.createUnit(1L, request));
        verify(organizationRepository, never()).findById(any());
        verify(unitRepository, never()).save(any());
    }

    @Test
    void getUnitThrowsWhenUnitIsMissingInOrganization() {
        when(unitRepository.findByIdAndOrganization_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(UnitNotFoundException.class, () -> service.getUnit(1L, 99L));
    }

    @Test
    void updateUnitThrowsWhenUnitIsInactive() {
        TemperatureUnit inactive = unit(12L, 1L, false);
        when(unitRepository.findByIdAndOrganization_Id(12L, 1L)).thenReturn(Optional.of(inactive));

        UnitRequest request = request(1.0, 3.0, 5.0);

        assertThrows(UnitInactiveException.class, () -> service.updateUnit(1L, 12L, request));
        verify(unitRepository, never()).save(any());
    }

    @Test
    void createUnitRestoresUnitAndPersistsIt() {
        Organization organization = Organization.builder()
                .id(1L)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        TemperatureUnit mapped = unit(null, null, false);
        mapped.setDeletedAt(java.time.LocalDateTime.now());

        UnitRequest request = request(1.0, 3.0, 5.0);
        UnitResponse expectedResponse = response(42L, true);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(unitMapper.toEntity(request)).thenReturn(mapped);
        when(unitRepository.save(any(TemperatureUnit.class))).thenAnswer(invocation -> {
            TemperatureUnit saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });
        when(unitMapper.toResponse(any(TemperatureUnit.class))).thenReturn(expectedResponse);

        UnitResponse actual = service.createUnit(1L, request);

        ArgumentCaptor<TemperatureUnit> captor = ArgumentCaptor.forClass(TemperatureUnit.class);
        verify(unitRepository).save(captor.capture());
        TemperatureUnit persisted = captor.getValue();

        assertEquals(organization.getId(), persisted.getOrganization().getId());
        assertTrue(persisted.isActive());
        assertEquals(expectedResponse, actual);
    }

    @Test
    void updateStatusFalseMarksUnitAsDeleted() {
        TemperatureUnit active = unit(50L, 1L, true);
        when(unitRepository.findByIdAndOrganization_Id(50L, 1L)).thenReturn(Optional.of(active));
        when(unitRepository.save(any(TemperatureUnit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitMapper.toResponse(any(TemperatureUnit.class))).thenReturn(response(50L, false));

        UnitResponse response = service.updateStatus(1L, 50L, new UnitStatusRequest(false));

        assertEquals(50L, response.id());
        assertEquals(false, response.active());
    }

    private static UnitRequest request(double min, double target, double max) {
        return new UnitRequest(
                "Freezer A",
                UnitType.FREEZER,
                target,
                min,
                max,
                "Main storage"
        );
    }

    private static TemperatureUnit unit(Long id, Long organizationId, boolean active) {
        Organization organization = organizationId == null
                ? null
                : Organization.builder()
                .id(organizationId)
                .name("Everest")
                .organizationNumber("123456789")
                .build();

        TemperatureUnit unit = TemperatureUnit.builder()
                .id(id)
                .organization(organization)
                .name("Freezer A")
                .type(UnitType.FREEZER)
                .targetTemperature(3.0)
                .minThreshold(1.0)
                .maxThreshold(5.0)
                .description("Main storage")
                .active(active)
                .build();

        if (!active) {
            unit.setDeletedAt(java.time.LocalDateTime.now());
        }

        return unit;
    }

    private static UnitResponse response(Long id, boolean active) {
        return new UnitResponse(
                id,
                1L,
                "Freezer A",
                UnitType.FREEZER,
                3.0,
                1.0,
                5.0,
                "Main storage",
                active,
                null,
                null,
                null
        );
    }
}