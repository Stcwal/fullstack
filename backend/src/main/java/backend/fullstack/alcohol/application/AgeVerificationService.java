package backend.fullstack.alcohol.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.alcohol.api.dto.AgeVerificationRequest;
import backend.fullstack.alcohol.api.dto.AgeVerificationResponse;
import backend.fullstack.alcohol.domain.AgeVerificationLog;
import backend.fullstack.alcohol.infrastructure.AgeVerificationRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.Location;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;

/**
 * Service for logging and querying age verification checks
 * as required by Norwegian alcohol law (Alkoholloven).
 */
@Service
public class AgeVerificationService {

    private final AgeVerificationRepository verificationRepository;
    private final OrganizationRepository organizationRepository;
    private final LocationRepository locationRepository;
    private final AccessContextService accessContext;

    public AgeVerificationService(
            AgeVerificationRepository verificationRepository,
            OrganizationRepository organizationRepository,
            LocationRepository locationRepository,
            AccessContextService accessContext
    ) {
        this.verificationRepository = verificationRepository;
        this.organizationRepository = organizationRepository;
        this.locationRepository = locationRepository;
        this.accessContext = accessContext;
    }

    @Transactional
    public AgeVerificationResponse create(AgeVerificationRequest request) {
        Long orgId = accessContext.getCurrentOrganizationId();
        User currentUser = accessContext.getCurrentUser();

        accessContext.assertCanAccess(request.getLocationId());

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        Location location = findLocationInOrganization(request.getLocationId(), orgId);

        AgeVerificationLog log = AgeVerificationLog.builder()
                .organization(organization)
                .location(location)
                .verifiedBy(currentUser)
                .verificationMethod(request.getVerificationMethod())
                .guestAppearedUnderage(request.isGuestAppearedUnderage())
                .idWasValid(request.getIdWasValid())
                .wasRefused(request.isWasRefused())
                .note(request.getNote())
                .verifiedAt(request.getVerifiedAt() != null ? request.getVerifiedAt() : LocalDateTime.now())
                .build();

        return toResponse(verificationRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<AgeVerificationResponse> list(Long locationId, LocalDateTime from, LocalDateTime to) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return verificationRepository.findFiltered(orgId, locationId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgeVerificationResponse getById(Long id) {
        return toResponse(findInCurrentOrganization(id));
    }

    private AgeVerificationLog findInCurrentOrganization(Long id) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return verificationRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Age verification log not found"));
    }

    private Location findLocationInOrganization(Long locationId, Long orgId) {
        return locationRepository.findById(locationId)
                .filter(loc -> orgId.equals(loc.getOrganizationId()))
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
    }

    private AgeVerificationResponse toResponse(AgeVerificationLog log) {
        User verifier = log.getVerifiedBy();
        Location location = log.getLocation();
        return AgeVerificationResponse.builder()
                .id(log.getId())
                .organizationId(log.getOrganizationId())
                .locationId(log.getLocationId())
                .locationName(location != null ? location.getName() : null)
                .verifiedByUserId(log.getVerifiedByUserId())
                .verifiedByName(verifier != null ? verifier.getFirstName() + " " + verifier.getLastName() : null)
                .verificationMethod(log.getVerificationMethod())
                .guestAppearedUnderage(log.isGuestAppearedUnderage())
                .idWasValid(log.getIdWasValid())
                .wasRefused(log.isWasRefused())
                .note(log.getNote())
                .verifiedAt(log.getVerifiedAt())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
