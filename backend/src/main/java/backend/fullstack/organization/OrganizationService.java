package backend.fullstack.organization;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.organization.dto.OrganizationRequest;
import backend.fullstack.organization.dto.OrganizationResponse;
import backend.fullstack.user.AccessContextService;
import lombok.RequiredArgsConstructor;



/**
 * Service class for managing organizations.
 *
 * @version 1.0
 * @since 28.03.26
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final AccessContextService accessContext;
    private final OrganizationMapper organizationMapper;

    public OrganizationResponse create(OrganizationRequest request) {
        if (organizationRepository.existsByOrganizationNumber(request.getOrganizationNumber())) {
            throw new OrganizationConflictException(
                    "An organization with org number " + request.getOrganizationNumber() + " already exists"
            );
        }
        Organization org = Organization.builder()
                .name(request.getName())
                .organizationNumber(request.getOrganizationNumber())
                .build();
        return organizationMapper.toResponse(organizationRepository.save(org));
    }

    public OrganizationResponse getById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Users can only see their own organization
        if (!org.getId().equals(accessContext.getCurrentOrganizationId())) {
            throw new AccessDeniedException("No access to this organization");
        }
        return organizationMapper.toResponse(org);
    }
}