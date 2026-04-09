package backend.fullstack.alcohol.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.alcohol.api.dto.AlcoholLicenseRequest;
import backend.fullstack.alcohol.api.dto.AlcoholLicenseResponse;
import backend.fullstack.alcohol.domain.AlcoholLicense;
import backend.fullstack.alcohol.infrastructure.AlcoholLicenseRepository;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;

/**
 * Service for managing alcohol licenses (bevillinger) for an organization.
 */
@Service
public class AlcoholLicenseService {

    private final AlcoholLicenseRepository licenseRepository;
    private final OrganizationRepository organizationRepository;
    private final AccessContextService accessContext;

    public AlcoholLicenseService(
            AlcoholLicenseRepository licenseRepository,
            OrganizationRepository organizationRepository,
            AccessContextService accessContext
    ) {
        this.licenseRepository = licenseRepository;
        this.organizationRepository = organizationRepository;
        this.accessContext = accessContext;
    }

    @Transactional(readOnly = true)
    public List<AlcoholLicenseResponse> listLicenses() {
        Long orgId = accessContext.getCurrentOrganizationId();
        return licenseRepository.findByOrganization_IdOrderByExpiresAtDesc(orgId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlcoholLicenseResponse getById(Long id) {
        return toResponse(findInCurrentOrganization(id));
    }

    @Transactional
    public AlcoholLicenseResponse create(AlcoholLicenseRequest request) {
        Long orgId = accessContext.getCurrentOrganizationId();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        AlcoholLicense license = AlcoholLicense.builder()
                .organization(organization)
                .licenseType(request.getLicenseType())
                .licenseNumber(request.getLicenseNumber())
                .issuedAt(request.getIssuedAt())
                .expiresAt(request.getExpiresAt())
                .issuingAuthority(request.getIssuingAuthority())
                .notes(request.getNotes())
                .build();

        return toResponse(licenseRepository.save(license));
    }

    @Transactional
    public AlcoholLicenseResponse update(Long id, AlcoholLicenseRequest request) {
        AlcoholLicense license = findInCurrentOrganization(id);

        license.setLicenseType(request.getLicenseType());
        license.setLicenseNumber(request.getLicenseNumber());
        license.setIssuedAt(request.getIssuedAt());
        license.setExpiresAt(request.getExpiresAt());
        license.setIssuingAuthority(request.getIssuingAuthority());
        license.setNotes(request.getNotes());

        return toResponse(licenseRepository.save(license));
    }

    @Transactional
    public void delete(Long id) {
        AlcoholLicense license = findInCurrentOrganization(id);
        licenseRepository.delete(license);
    }

    private AlcoholLicense findInCurrentOrganization(Long id) {
        Long orgId = accessContext.getCurrentOrganizationId();
        return licenseRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Alcohol license not found"));
    }

    private AlcoholLicenseResponse toResponse(AlcoholLicense license) {
        return AlcoholLicenseResponse.builder()
                .id(license.getId())
                .organizationId(license.getOrganizationId())
                .licenseType(license.getLicenseType())
                .licenseNumber(license.getLicenseNumber())
                .issuedAt(license.getIssuedAt())
                .expiresAt(license.getExpiresAt())
                .issuingAuthority(license.getIssuingAuthority())
                .notes(license.getNotes())
                .expired(license.isExpired())
                .createdAt(license.getCreatedAt())
                .updatedAt(license.getUpdatedAt())
                .build();
    }
}
