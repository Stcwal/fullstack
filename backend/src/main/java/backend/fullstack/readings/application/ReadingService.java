package backend.fullstack.readings.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.readings.api.dto.ReadingMapper;
import backend.fullstack.readings.api.dto.ReadingRequest;
import backend.fullstack.readings.api.dto.ReadingResponse;
import backend.fullstack.readings.domain.TemperatureReading;
import backend.fullstack.readings.infrastructure.TemperatureReadingRepository;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@Service
@Transactional
public class ReadingService {

    private final TemperatureReadingRepository readingRepository;
    private final TemperatureUnitRepository unitRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ReadingMapper readingMapper;

    public ReadingService(
            TemperatureReadingRepository readingRepository,
            TemperatureUnitRepository unitRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            ReadingMapper readingMapper
    ) {
        this.readingRepository = readingRepository;
        this.unitRepository = unitRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.readingMapper = readingMapper;
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> getReadingsForUnit(Long organizationId, Long unitId) {
        unitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));

        return readingRepository.findByUnit_IdAndOrganization_IdOrderByRecordedAtDesc(unitId, organizationId)
                .stream()
                .map(readingMapper::toResponse)
                .toList();
    }

    public ReadingResponse recordReading(Long organizationId, Long userId, Long unitId, ReadingRequest request) {
        TemperatureUnit unit = unitRepository.findByIdAndOrganization_Id(unitId, organizationId)
                .orElseThrow(() -> new UnitNotFoundException(unitId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

        boolean outOfRange = request.temperature() < unit.getMinThreshold()
                || request.temperature() > unit.getMaxThreshold();

        TemperatureReading reading = TemperatureReading.builder()
                .unit(unit)
                .organization(organization)
                .recordedBy(user)
                .temperature(request.temperature())
                .recordedAt(request.recordedAt())
                .isOutOfRange(outOfRange)
                .note(request.note())
                .build();

        TemperatureReading saved = readingRepository.save(reading);
        return readingMapper.toResponse(saved);
    }
}
