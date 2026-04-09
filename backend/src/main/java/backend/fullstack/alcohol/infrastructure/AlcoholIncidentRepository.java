package backend.fullstack.alcohol.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.fullstack.alcohol.domain.AlcoholServingIncident;
import backend.fullstack.alcohol.domain.IncidentStatus;
import backend.fullstack.alcohol.domain.IncidentType;

public interface AlcoholIncidentRepository extends JpaRepository<AlcoholServingIncident, Long> {

    Optional<AlcoholServingIncident> findByIdAndOrganization_Id(Long id, Long organizationId);

    @Query("""
        SELECT i
        FROM AlcoholServingIncident i
        WHERE i.organization.id = :organizationId
          AND (:locationId IS NULL OR i.location.id = :locationId)
          AND (:status IS NULL OR i.status = :status)
          AND (:incidentType IS NULL OR i.incidentType = :incidentType)
          AND (:from IS NULL OR i.occurredAt >= :from)
          AND (:to IS NULL OR i.occurredAt <= :to)
        ORDER BY i.occurredAt DESC, i.id DESC
        """)
    List<AlcoholServingIncident> findFiltered(
            @Param("organizationId") Long organizationId,
            @Param("locationId") Long locationId,
            @Param("status") IncidentStatus status,
            @Param("incidentType") IncidentType incidentType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    long countByOrganization_IdAndStatus(Long organizationId, IncidentStatus status);
}
