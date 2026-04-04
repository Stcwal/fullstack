package backend.fullstack.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Organization entities.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByOrganizationNumber(String organizationNumber);

    boolean existsByOrganizationNumberAndIdNot(String organizationNumber, Long id);

    Organization findByOrganizationNumber(String organizationNumber);
}
