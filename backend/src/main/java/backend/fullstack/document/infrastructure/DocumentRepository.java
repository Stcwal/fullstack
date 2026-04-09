package backend.fullstack.document.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.fullstack.document.domain.Document;
import backend.fullstack.document.domain.DocumentCategory;

/**
 * Repository for {@link Document} entities.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Finds all documents for an organization, ordered by most recent first.
     */
    List<Document> findByOrganization_IdOrderByCreatedAtDesc(Long organizationId);

    /**
     * Finds all documents for an organization filtered by category.
     */
    List<Document> findByOrganization_IdAndCategoryOrderByCreatedAtDesc(
            Long organizationId, DocumentCategory category);

    /**
     * Finds a single document by ID scoped to an organization.
     */
    Optional<Document> findByIdAndOrganization_Id(Long id, Long organizationId);
}
