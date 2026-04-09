package backend.fullstack.organization;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;
import backend.fullstack.organization.dto.OrganizationMapper;
import backend.fullstack.permission.core.AuthorizationService;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.UserRepository;
import lombok.RequiredArgsConstructor;



/**
 * Service class for managing organizations.
 *
 * @version 1.1
 * @since 03.04.26
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AccessContextService accessContext;
    private final AuthorizationService authorizationService;
    private final OrganizationMapper organizationMapper;

    public OrganizationResponse create(OrganizationRequest request) {
        if (userRepository.count() > 0) {
            throw new AccessDeniedException("Organization registration is closed after initial bootstrap");
        }
        if (organizationRepository.existsByOrganizationNumber(request.getOrganizationNumber())) {
            throw new OrganizationConflictException(
                    "An organization with org number " + request.getOrganizationNumber() + " already exists"
            );
        }

        Organization org = organizationMapper.toEntity(request);

        return organizationMapper.toResponse(organizationRepository.save(org));
    }

    public OrganizationResponse getCurrentOrganization() {
        authorizationService.assertPermission(Permission.ORGANIZATION_SETTINGS_READ);

        Long orgId = accessContext.getCurrentOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        return organizationMapper.toResponse(org);
    }

    public OrganizationResponse getById(Long id) {
        authorizationService.assertPermission(Permission.ORGANIZATION_SETTINGS_READ);

        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Users can only see their own organization
        if (!org.getId().equals(accessContext.getCurrentOrganizationId())) {
            throw new AccessDeniedException("No access to this organization");
        }

        return organizationMapper.toResponse(org);
    }

    public OrganizationResponse updateCurrentOrganization(OrganizationRequest request) {
        authorizationService.assertPermission(Permission.ORGANIZATION_SETTINGS_UPDATE);

        Long orgId = accessContext.getCurrentOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (organizationRepository.existsByOrganizationNumberAndIdNot(request.getOrganizationNumber(), orgId)) {
            throw new OrganizationConflictException(
                    "An organization with org number " + request.getOrganizationNumber() + " already exists"
            );
        }

        org.setName(request.getName());
        org.setOrganizationNumber(request.getOrganizationNumber());

        return organizationMapper.toResponse(organizationRepository.save(org));
    }
}
