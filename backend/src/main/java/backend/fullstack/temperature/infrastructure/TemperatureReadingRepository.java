package backend.fullstack.temperature.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.fullstack.temperature.domain.TemperatureReading;

@Repository
public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {

    List<TemperatureReading> findByOrganization_IdAndUnit_IdOrderByRecordedAtDesc(Long organizationId, Long unitId);

    Optional<TemperatureReading> findByIdAndOrganization_Id(Long id, Long organizationId);

    @Query("""
            SELECT r
            FROM TemperatureReading r
            WHERE r.organization.id = :organizationId
              AND (:unitId IS NULL OR r.unit.id = :unitId)
              AND (:from IS NULL OR r.recordedAt >= :from)
              AND (:to IS NULL OR r.recordedAt <= :to)
              AND (:deviationsOnly = false OR r.isDeviation = true)
            ORDER BY r.recordedAt DESC
            """)
    Page<TemperatureReading> findAllByOrganizationWithFilters(
            @Param("organizationId") Long organizationId,
            @Param("unitId") Long unitId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("deviationsOnly") boolean deviationsOnly,
            Pageable pageable
    );

    @Query("""
            SELECT r
            FROM TemperatureReading r
            WHERE r.organization.id = :organizationId
              AND r.unit.id = :unitId
              AND (:from IS NULL OR r.recordedAt >= :from)
              AND (:to IS NULL OR r.recordedAt <= :to)
            ORDER BY r.recordedAt DESC
            """)
    List<TemperatureReading> findByOrganizationAndUnitAndRecordedAtBetween(
            @Param("organizationId") Long organizationId,
            @Param("unitId") Long unitId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long countByOrganization_IdAndIsDeviationTrue(Long organizationId);

    long countByOrganization_IdAndIsDeviationTrueAndRecordedAtAfter(Long organizationId, LocalDateTime since);
}