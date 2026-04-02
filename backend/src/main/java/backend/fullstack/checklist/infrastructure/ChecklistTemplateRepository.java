package backend.fullstack.checklist.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.fullstack.checklist.domain.ChecklistTemplate;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    List<ChecklistTemplate> findAllByOrganizationId(Long organizationId);

    Optional<ChecklistTemplate> findByIdAndOrganizationId(Long id, Long organizationId);
}
