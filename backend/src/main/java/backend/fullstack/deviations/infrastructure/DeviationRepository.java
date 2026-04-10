package backend.fullstack.deviations.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationStatus;

@Repository
public interface DeviationRepository extends JpaRepository<Deviation, Long> {

    List<Deviation> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    List<Deviation> findByOrganization_IdAndStatusOrderByCreatedAtDesc(Long organizationId, DeviationStatus status);

    Optional<Deviation> findByIdAndOrganization_Id(Long id, Long organizationId);

    long countByOrganization_IdAndStatus(Long organizationId, DeviationStatus status);

    @Query("""
            SELECT COUNT(d) FROM Deviation d
            WHERE d.organization.id = :organizationId
              AND d.status = :status
              AND (:locationId IS NULL OR d.location.id = :locationId)
            """)
    long countByOrganizationAndStatusAndOptionalLocation(
            @Param("organizationId") Long organizationId,
            @Param("status") DeviationStatus status,
            @Param("locationId") Long locationId
    );

    @Query("""
            SELECT d FROM Deviation d
            WHERE d.organization.id = :organizationId
              AND d.status = :status
              AND (:locationId IS NULL OR d.location.id = :locationId)
            ORDER BY d.createdAt DESC
            """)
    List<Deviation> findByOrganizationAndStatusAndOptionalLocation(
            @Param("organizationId") Long organizationId,
            @Param("status") DeviationStatus status,
            @Param("locationId") Long locationId
    );
}
