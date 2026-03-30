package backend.fullstack.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Location entities.
 * Provides methods to perform CRUD operations and custom queries related to locations within an organization.
 *
 * @version 1.0
 * @since 27.03.26
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByOrganization_Id(Long organizationId);


    @Query("SELECT l.id FROM Location l WHERE l.organization.id = :organizationId")
    List<Long> findIdsByOrganizationId(@Param("organizationId") Long organizationId);


    // Used to verify that a location belong to the same organization as the user when updating or deleting a location
    Optional<Location> findByIdAndOrganization_Id(Long id, Long organizationId);

    boolean existsByIdAndOrganization_Id(Long id, Long organizationId);

    Optional<Location> findByIdAndOrganizationId(Long id, Long organizationId);

}