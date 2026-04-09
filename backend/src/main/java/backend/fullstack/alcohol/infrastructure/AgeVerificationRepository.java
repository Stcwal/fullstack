package backend.fullstack.alcohol.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.fullstack.alcohol.domain.AgeVerificationLog;

public interface AgeVerificationRepository extends JpaRepository<AgeVerificationLog, Long> {

    Optional<AgeVerificationLog> findByIdAndOrganization_Id(Long id, Long organizationId);

    @Query("""
        SELECT a
        FROM AgeVerificationLog a
        WHERE a.organization.id = :organizationId
          AND (:locationId IS NULL OR a.location.id = :locationId)
          AND (:from IS NULL OR a.verifiedAt >= :from)
          AND (:to IS NULL OR a.verifiedAt <= :to)
        ORDER BY a.verifiedAt DESC, a.id DESC
        """)
    List<AgeVerificationLog> findFiltered(
            @Param("organizationId") Long organizationId,
            @Param("locationId") Long locationId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT COUNT(a)
        FROM AgeVerificationLog a
        WHERE a.organization.id = :organizationId
          AND a.verifiedAt >= :from
          AND a.verifiedAt <= :to
        """)
    long countByOrganizationAndPeriod(
            @Param("organizationId") Long organizationId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
