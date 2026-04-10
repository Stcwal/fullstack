package backend.fullstack.units.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import backend.fullstack.units.domain.TemperatureUnit;

@Repository
public interface TemperatureUnitRepository extends JpaRepository<TemperatureUnit, Long> {

    List<TemperatureUnit> findByOrganization_IdOrderByNameAsc(Long organizationId);

    List<TemperatureUnit> findByOrganization_IdAndActiveOrderByNameAsc(Long organizationId, boolean active);

    Optional<TemperatureUnit> findByIdAndOrganization_Id(Long id, Long organizationId);

    @Query("""
            SELECT u
            FROM TemperatureUnit u
            WHERE u.organization.id = :organizationId
              AND (:active IS NULL OR u.active = :active)
              AND (:locationId IS NULL OR u.location.id = :locationId)
            ORDER BY u.name ASC
            """)
    List<TemperatureUnit> findByOrganizationAndOptionalActive(
            @Param("organizationId") Long organizationId,
            @Param("active") Boolean active,
            @Param("locationId") Long locationId
    );
}
