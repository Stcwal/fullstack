package backend.fullstack.checklist.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.fullstack.checklist.domain.ChecklistInstance;

public interface ChecklistInstanceRepository extends JpaRepository<ChecklistInstance, Long> {

    List<ChecklistInstance> findAllByOrganizationId(Long organizationId);

    Optional<ChecklistInstance> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("""
            SELECT ci FROM ChecklistInstance ci
            WHERE ci.organizationId = :organizationId
              AND (:locationId IS NULL OR ci.locationId = :locationId)
            """)
    List<ChecklistInstance> findAllByOrganizationIdAndOptionalLocation(
            @Param("organizationId") Long organizationId,
            @Param("locationId") Long locationId
    );
}
