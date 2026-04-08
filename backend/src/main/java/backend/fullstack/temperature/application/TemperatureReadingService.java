package backend.fullstack.temperature.application;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.temperature.api.dto.TemperatureReadingDeviationResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingMapper;
import backend.fullstack.temperature.api.dto.TemperatureReadingRequest;
import backend.fullstack.temperature.api.dto.TemperatureReadingResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsGroupBy;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsPointResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsResponse;
import backend.fullstack.temperature.api.dto.TemperatureReadingStatsSeriesResponse;
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

        @Transactional(readOnly = true)
        public TemperatureReadingStatsResponse getReadingStats(
            Long organizationId,
            List<Long> unitIds,
            LocalDateTime from,
            LocalDateTime to,
            TemperatureReadingStatsGroupBy groupBy
        ) {
        List<Long> distinctUnitIds = sanitizeUnitIds(unitIds);
        List<TemperatureReading> readings = distinctUnitIds == null
            ? readingRepository.findForStatsByOrganizationAndRange(organizationId, from, to)
            : readingRepository.findForStatsByOrganizationAndUnitIdsAndRange(organizationId, distinctUnitIds, from, to);

        Map<Long, List<TemperatureReading>> readingsByUnit = readings.stream()
            .collect(Collectors.groupingBy(reading -> reading.getUnit().getId()));

        List<TemperatureReadingStatsSeriesResponse> series = readingsByUnit.values().stream()
            .sorted(Comparator.comparing(unitReadings -> unitReadings.get(0).getUnit().getName(), String.CASE_INSENSITIVE_ORDER))
            .map(unitReadings -> toSeries(unitReadings, groupBy))
            .toList();

        List<TemperatureReadingDeviationResponse> deviations = readings.stream()
            .filter(TemperatureReading::isDeviation)
            .sorted(Comparator.comparing(TemperatureReading::getRecordedAt).reversed())
            .map(this::toDeviationResponse)
            .toList();

        return new TemperatureReadingStatsResponse(series, deviations);
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

    private List<Long> sanitizeUnitIds(List<Long> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return null;
        }

        List<Long> sanitized = unitIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        return sanitized.isEmpty() ? null : sanitized;
    }

    private TemperatureReadingStatsSeriesResponse toSeries(
            List<TemperatureReading> unitReadings,
            TemperatureReadingStatsGroupBy groupBy
    ) {
        TemperatureReading firstReading = unitReadings.get(0);
        Map<LocalDateTime, List<TemperatureReading>> groupedByTime = unitReadings.stream()
                .collect(Collectors.groupingBy(
                        reading -> truncateTimestamp(reading.getRecordedAt(), groupBy),
                        java.util.TreeMap::new,
                        Collectors.toList()
                ));

        List<TemperatureReadingStatsPointResponse> dataPoints = groupedByTime.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .map(TemperatureReading::getTemperature)
                            .filter(java.util.Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0d);

                    boolean hasDeviation = entry.getValue().stream().anyMatch(TemperatureReading::isDeviation);
                    return new TemperatureReadingStatsPointResponse(entry.getKey(), avg, hasDeviation);
                })
                .toList();

        return new TemperatureReadingStatsSeriesResponse(
                firstReading.getUnit().getId(),
                firstReading.getUnit().getName(),
                dataPoints
        );
    }

    private TemperatureReadingDeviationResponse toDeviationResponse(TemperatureReading reading) {
        return new TemperatureReadingDeviationResponse(
                reading.getId(),
                reading.getUnit().getId(),
                reading.getUnit().getName(),
                reading.getTemperature(),
                resolveBreachedThreshold(reading),
                reading.getRecordedAt()
        );
    }

    private Double resolveBreachedThreshold(TemperatureReading reading) {
        if (reading.getUnit() == null || reading.getTemperature() == null) {
            return null;
        }

        Double minThreshold = reading.getUnit().getMinThreshold();
        Double maxThreshold = reading.getUnit().getMaxThreshold();

        if (minThreshold != null && reading.getTemperature() < minThreshold) {
            return minThreshold;
        }

        if (maxThreshold != null && reading.getTemperature() > maxThreshold) {
            return maxThreshold;
        }

        return null;
    }

    private LocalDateTime truncateTimestamp(LocalDateTime timestamp, TemperatureReadingStatsGroupBy groupBy) {
        return switch (groupBy) {
            case HOUR -> timestamp.withMinute(0).withSecond(0).withNano(0);
            case DAY -> timestamp.toLocalDate().atStartOfDay();
            case WEEK -> timestamp.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    .atStartOfDay();
        };
    }
}