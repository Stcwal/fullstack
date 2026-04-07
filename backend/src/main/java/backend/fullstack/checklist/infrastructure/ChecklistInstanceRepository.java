package backend.fullstack.checklist.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.fullstack.checklist.domain.ChecklistInstance;

public interface ChecklistInstanceRepository extends JpaRepository<ChecklistInstance, Long> {

    List<ChecklistInstance> findAllByOrganizationId(Long organizationId);

    Optional<ChecklistInstance> findByIdAndOrganizationId(Long id, Long organizationId);
}
