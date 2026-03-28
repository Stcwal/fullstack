package backend.fullstack.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import backend.fullstack.location.LocationRepository;

/**
 * Centralized tenant and location access resolver.
 * All location-scoped services should use this service to prevent duplicated
 * authorization logic and role drift across modules.
 */
@Service
public class AccessContextService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    public AccessContextService(UserRepository userRepository, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Returns a list of location IDs that the current user has access to, based on their role and permissions.
     * - ADMIN users have access to all locations within their organization.
     *
     * @return List of accessible location IDs for the current user.
     */
    public List<Long> getAllowedLocationIds() {
        User user = getCurrentUser();

        return switch (user.getRole()) {
            case ADMIN -> locationRepository.findIdsByOrganizationId(user.getOrganizationId());
            case SUPERVISOR -> userRepository.findAdditionalLocationIdsByUserId(user.getId());
            case MANAGER, STAFF -> resolveStaffOrManagerLocations(user);
        };
    }

    /**
     * Checks if the current user has access to the specified location ID. If the location ID is null or the user does not have access, an AccessDeniedException is thrown.
     *
     * @param locationId The ID of the location to check access for.
     * @throws AccessDeniedException if the location ID is null or the user does not have access to the specified location.
     */
    public void assertCanAccess(Long locationId) {
        if (locationId == null) {
            throw new AccessDeniedException("Location id is required");
        }

        if (!getAllowedLocationIds().contains(locationId)) {
            throw new AccessDeniedException("No access to this location");
        }
    }

    /**
     * Returns the organization ID of the current user.
     *
     * @return The organization ID of the current user.
     */
    public Long getCurrentOrganizationId() {
        return getCurrentUser().getOrganizationId();
    }

    /**
     * Returns the current authenticated user. If there is no authenticated user or the user cannot be found in the database, an AccessDeniedException is thrown.
     *
     * @return The current authenticated User entity.
     * @throws AccessDeniedException if the request is unauthenticated or the authenticated user cannot be found in the database.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated request");
        }

        String email = authentication.getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    /**
     * Resolves the location IDs for STAFF and MANAGER roles by combining their home location and any additional locations they have access to, ensuring no duplicates.
     *
     * @param user The user for whom to resolve location IDs. Must have a role of STAFF or MANAGER.
     * @return A list of unique location IDs that the user has access to, including their home location and any additional locations.
     */
    private List<Long> resolveStaffOrManagerLocations(User user) {
        Set<Long> uniqueLocationIds = new LinkedHashSet<>();

        if (user.getHomeLocationId() != null) {
            uniqueLocationIds.add(user.getHomeLocationId());
        }

        uniqueLocationIds.addAll(userRepository.findAdditionalLocationIdsByUserId(user.getId()));

        return new ArrayList<>(uniqueLocationIds);
    }
}
