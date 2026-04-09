package backend.fullstack.deviations.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.fullstack.deviations.domain.DeviationComment;

@Repository
public interface DeviationCommentRepository extends JpaRepository<DeviationComment, Long> {

    List<DeviationComment> findByOrganization_IdAndDeviation_IdOrderByCreatedAtAsc(Long organizationId, Long deviationId);
}