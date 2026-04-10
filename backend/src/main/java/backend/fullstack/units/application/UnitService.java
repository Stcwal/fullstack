package backend.fullstack.units.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.exceptions.InvalidThresholdException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.units.api.dto.UnitMapper;
import backend.fullstack.units.api.dto.UnitRequest;
import backend.fullstack.units.api.dto.UnitResponse;
import backend.fullstack.units.api.dto.UnitStatusRequest;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;

@Service
@Transactional
public class UnitService {

    private final TemperatureUnitRepository unitRepository;
    private final OrganizationRepository organizationRepository;
    private final UnitMapper unitMapper;

    public UnitService(
            TemperatureUnitRepository unitRepository,
            OrganizationRepository organizationRepository,
            UnitMapper unitMapper
    ) {
        this.unitRepository = unitRepository;
        this.organizationRepository = organizationRepository;
        this.unitMapper = unitMapper;
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> getUnits(Long organizationId, Boolean active, Long locationId) {
        return unitRepository.findByOrganizationAndOptionalActive(organizationId, active, locationId).stream()
                .map(unitMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnitResponse getUnit(Long organizationId, Long unitId) {
        TemperatureUnit unit = findScopedUnit(organizationId, unitId);
        return unitMapper.toResponse(unit);
    }

    public UnitResponse createUnit(Long organizationId, UnitRequest request) {
        validateThresholds(request.minThreshold(), request.targetTemperature(), request.maxThreshold());

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        TemperatureUnit unit = unitMapper.toEntity(request);
        unit.setOrganization(organization);
        unit.restore();

        TemperatureUnit saved = unitRepository.save(unit);
        return unitMapper.toResponse(saved);
    }

    public UnitResponse updateUnit(Long organizationId, Long unitId, UnitRequest request) {
        validateThresholds(request.minThreshold(), request.targetTemperature(), request.maxThreshold());

        TemperatureUnit unit = findScopedUnit(organizationId, unitId);
        assertUnitActive(unit);
        applyRequest(unit, request);

        TemperatureUnit saved = unitRepository.save(unit);
        return unitMapper.toResponse(saved);
    }

    public UnitResponse updateStatus(Long organizationId, Long unitId, UnitStatusRequest request) {
        TemperatureUnit unit = findScopedUnit(organizationId, unitId);

        if (Boolean.TRUE.equals(request.active())) {
            unit.restore();
        } else {
            unit.markAsDeleted();
        }

        TemperatureUnit saved = unitRepository.save(unit);
        return unitMapper.toResponse(saved);
    }

    public UnitResponse softDelete(Long organizationId, Long unitId) {
        TemperatureUnit unit = findScopedUnit(organizationId, unitId);
        unit.markAsDeleted();

        TemperatureUnit saved = unitRepository.save(unit);
        return unitMapper.toResponse(saved);
    }

    private TemperatureUnit findScopedUnit(Long organizationId, Long unitId) {
        return unitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));
    }

    private void assertUnitActive(TemperatureUnit unit) {
        if (!unit.isActive()) {
            throw new UnitInactiveException(unit.getId());
        }
    }

    private void applyRequest(TemperatureUnit unit, UnitRequest request) {
        unit.setName(request.name());
        unit.setType(request.type());
        unit.setTargetTemperature(request.targetTemperature());
        unit.setMinThreshold(request.minThreshold());
        unit.setMaxThreshold(request.maxThreshold());
        unit.setDescription(request.description());
    }

    private void validateThresholds(Double min, Double target, Double max) {
        if (min == null || target == null || max == null) {
            return;
        }

        if (!(min < target && target < max)) {
            throw new InvalidThresholdException();
        }
    }
}
