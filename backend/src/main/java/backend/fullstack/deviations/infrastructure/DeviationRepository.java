package backend.fullstack.deviations.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationStatus;

@Repository
public interface DeviationRepository extends JpaRepository<Deviation, Long> {

    List<Deviation> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    List<Deviation> findByOrganization_IdAndStatusOrderByCreatedAtDesc(Long organizationId, DeviationStatus status);

    Optional<Deviation> findByIdAndOrganization_Id(Long id, Long organizationId);

    long countByOrganization_IdAndStatus(Long organizationId, DeviationStatus status);
}
