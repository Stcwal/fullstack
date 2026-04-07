package backend.fullstack.temperature.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.temperature.api.dto.TemperatureReadingMapper;
import backend.fullstack.temperature.api.dto.TemperatureReadingRequest;
import backend.fullstack.temperature.api.dto.TemperatureReadingResponse;
import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@Service
@Transactional
public class TemperatureReadingService {

    private final TemperatureReadingRepository readingRepository;
    private final TemperatureUnitRepository unitRepository;
    private final UserRepository userRepository;
    private final TemperatureReadingMapper readingMapper;

    public TemperatureReadingService(
            TemperatureReadingRepository readingRepository,
            TemperatureUnitRepository unitRepository,
            UserRepository userRepository,
            TemperatureReadingMapper readingMapper
    ) {
        this.readingRepository = readingRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
        this.readingMapper = readingMapper;
    }

    @Transactional(readOnly = true)
    public List<TemperatureReadingResponse> listUnitReadings(
            Long organizationId,
            Long unitId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return readingRepository.findByOrganizationAndUnitAndRecordedAtBetween(organizationId, unitId, from, to)
                .stream()
                .map(readingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TemperatureReadingResponse> listReadings(
            Long organizationId,
            Long unitId,
            LocalDateTime from,
            LocalDateTime to,
            boolean deviationsOnly,
            Pageable pageable
    ) {
        return readingRepository.findAllByOrganizationWithFilters(organizationId, unitId, from, to, deviationsOnly, pageable)
                .map(readingMapper::toResponse);
    }

    public TemperatureReadingResponse createReading(
            JwtPrincipal principal,
            Long unitId,
            TemperatureReadingRequest request
    ) {
        TemperatureUnit unit = findScopedActiveUnit(principal.organizationId(), unitId);
        User recordedBy = findScopedUser(principal.userId(), principal.organizationId());

        TemperatureReading reading = readingMapper.toEntity(request);
        reading.setOrganization(unit.getOrganization());
        reading.setUnit(unit);
        reading.setRecordedBy(recordedBy);
        reading.setRecordedAt(reading.getRecordedAt() != null ? reading.getRecordedAt() : LocalDateTime.now());
        reading.evaluateDeviation(unit.getMinThreshold(), unit.getMaxThreshold());

        TemperatureReading saved = readingRepository.save(reading);
        return readingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TemperatureReadingResponse getReading(Long organizationId, Long readingId) {
        TemperatureReading reading = readingRepository.findByIdAndOrganization_Id(readingId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Temperature reading", readingId));
        return readingMapper.toResponse(reading);
    }

    private TemperatureUnit findScopedActiveUnit(Long organizationId, Long unitId) {
        TemperatureUnit unit = unitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));

        if (!unit.isActive()) {
            throw new UnitInactiveException(unit.getId());
        }

        return unit;
    }

    private User findScopedUser(Long userId, Long organizationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!organizationId.equals(user.getOrganizationId())) {
            throw new ResourceNotFoundException("User", userId);
        }

        return user;
    }
}