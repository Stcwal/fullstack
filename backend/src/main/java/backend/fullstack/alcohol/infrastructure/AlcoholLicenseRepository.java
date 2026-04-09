package backend.fullstack.alcohol.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.fullstack.alcohol.domain.AlcoholLicense;

public interface AlcoholLicenseRepository extends JpaRepository<AlcoholLicense, Long> {

    List<AlcoholLicense> findByOrganization_IdOrderByExpiresAtDesc(Long organizationId);

    Optional<AlcoholLicense> findByIdAndOrganization_Id(Long id, Long organizationId);
}
