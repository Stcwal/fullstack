package backend.fullstack.readings.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.fullstack.readings.domain.TemperatureReading;

@Repository
public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {

    List<TemperatureReading> findByUnit_IdAndOrganization_IdOrderByRecordedAtDesc(Long unitId, Long organizationId);

    long countByOrganization_IdAndIsOutOfRangeTrueAndRecordedAtAfter(Long organizationId, LocalDateTime since);

    long countByOrganization_IdAndRecordedAtAfter(Long organizationId, LocalDateTime since);
}
