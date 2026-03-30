package backend.fullstack.location;

import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.location.dto.LocationMapper;
import backend.fullstack.location.dto.LocationRequest;
import backend.fullstack.location.dto.LocationResponse;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing locations within an organization.
 *
 * @version 1.0
 * @since 28.03.26
 */
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final OrganizationRepository organizationRepository;
    private final AccessContextService accessContext;
    private final LocationMapper locationMapper;



    public LocationResponse create(LocationRequest request) {
        accessContext.assertHasRole(Role.ADMIN);

        Long orgId = accessContext.getCurrentOrganizationId();

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Location location = locationMapper.toEntity(request);
        location.setOrganization(org);

        return locationMapper.toResponse(locationRepository.save(location));
    }

    public List<LocationResponse> getAllAccessible() {
        List<Long> allowedIds = accessContext.getAllowedLocationIds();
        return locationRepository.findAllById(allowedIds)
                .stream()
                .map(locationMapper::toResponse)
                .toList();
    }

    public LocationResponse getById(Long id) {
        accessContext.assertCanAccess(id); // throws 403 if not allowed
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        return locationMapper.toResponse(location);
    }

    public LocationResponse update(Long id, LocationRequest request) {
        accessContext.assertHasRole(Role.ADMIN);
        accessContext.assertCanAccess(id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        return locationMapper.toResponse(locationRepository.save(location));
    }

    public void delete(Long id) {
        accessContext.assertHasRole(Role.ADMIN);
        accessContext.assertCanAccess(id);
        locationRepository.deleteById(id);
    }
}