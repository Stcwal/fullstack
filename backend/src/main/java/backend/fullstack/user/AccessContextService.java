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
 * 
 * @version 1.0
 * @since 30.03.26
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

<<<<<<< Updated upstream
=======
    /**
     * Returns the role of the current user.
     * 
     * @return The role of the current user.
     */
    public Role getCurrentRole() {
        JwtPrincipal principal = getJwtPrincipal();
        if (principal != null && principal.role() != null) {
            return principal.role();
        }

        return getCurrentUser().getRole();
    }

    /**
     * Asserts that the current user has one of the specified roles. If the user does not have any of the allowed roles, an AccessDeniedException is thrown.
     * 
     * @param allowedRoles Varargs of roles that are allowed to access a resource or perform an action. The method checks if the current user's role matches any of the allowed roles.
     * @throws AccessDeniedException if the current user's role does not match any of the allowed
     */
    public void assertHasRole(Role... allowedRoles) {
        Role currentRole = getCurrentRole();
        for (Role role : allowedRoles) {
            if (currentRole == role) {
                return;
            }
        }

        throw new AccessDeniedException("Insufficient role for this operation");
    }

>>>>>>> Stashed changes
    /**
     * Returns the current authenticated user. If there is no authenticated user or the user cannot be found in the database, an AccessDeniedException is thrown.
     *
     * @return The current authenticated User entity.
     * @throws AccessDeniedException if the request is unauthenticated or the authenticated user cannot be found in the database.
     */
    public User getCurrentUser() {
<<<<<<< Updated upstream
=======
        Authentication authentication = requireAuthentication();
        String email = resolveAuthenticatedEmail(authentication);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    /**
     * Asserts that the current user has permission to manage the specified target user. This typically means that the current user has a higher role than the target user and belongs to the same organization.
     *
     * @param targetUser The user that is being managed (e.g., updated, deactivated).
     * @throws AccessDeniedException if the current user does not have permission to manage the target user.
     */
    private Authentication requireAuthentication() {
>>>>>>> Stashed changes
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated request");
        }

<<<<<<< Updated upstream
        String email = authentication.getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
=======
        return authentication;
    }

    /**
     * Helper method to extract the JwtPrincipal from the current security context, if available. This allows access to JWT claims such as role, organizationId, and locationIds without needing to query the database for the User entity. If the principal is not a JwtPrincipal or if there is no authenticated user, this method returns null.
     * 
     * @return the JwtPrincipal containing JWT claims, or null if not available
     */
    private JwtPrincipal getJwtPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal;
        }

        return null;
    }

    /**
     * Resolves the email of the currently authenticated user from the Authentication object. It first checks if the principal is an instance of JwtPrincipal and extracts the email claim. If not, it checks if the principal is an instance of User and returns the email field. If neither case matches, it falls back to authentication.getName(), which typically returns the username (email in this application) used for authentication. This method ensures that we can retrieve the user's email regardless of whether they are authenticated via JWT or a traditional UserDetails-based mechanism.
     * 
     * @param authentication the Authentication object from which to extract the user's email
     * @return the email of the authenticated user
     */
    private String resolveAuthenticatedEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.email();
        }

        if (principal instanceof User user) {
            return user.getEmail();
        }

        return authentication.getName();
>>>>>>> Stashed changes
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
