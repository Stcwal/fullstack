package backend.fullstack.user;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.location.LocationRepository;
import backend.fullstack.user.role.Role;

/**
 * Centralized tenant and location access resolver.
 * All location-scoped services should use this service to prevent duplicated
 * authorization logic and role drift across modules.
 * 
 * @version 1.1
 * @since 30.03.26
 */
@Service
public class AccessContextService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;

    public AccessContextService(
            UserRepository userRepository,
            LocationRepository locationRepository,
            UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository
    ) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.userLocationScopeAssignmentRepository = userLocationScopeAssignmentRepository;
    }

    /**
     * Returns a list of location IDs that the current user has access to, based on their role and permissions.
     * - ADMIN users have access to all locations within their organization.
     *
     * @return List of accessible location IDs for the current user.
     */
    public List<Long> getAllowedLocationIds() {
        User user = getCurrentUser();
        Set<Long> uniqueLocationIds = new LinkedHashSet<>();

        switch (user.getRole()) {
            case ADMIN -> uniqueLocationIds.addAll(locationRepository.findIdsByOrganizationId(user.getOrganizationId()));
            case SUPERVISOR, MANAGER, STAFF -> uniqueLocationIds.addAll(userRepository.findEffectiveLocationScopeByUserId(user.getId()));
        }

        // Temporary scope assignments are resolved from the database at request-time
        // so access changes take effect immediately without requiring re-login.
        uniqueLocationIds.addAll(
                userLocationScopeAssignmentRepository.findActiveLocationIdsByUserId(user.getId(), LocalDateTime.now())
        );

        return new ArrayList<>(uniqueLocationIds);
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
        JwtPrincipal principal = getJwtPrincipal();
        if (principal != null && principal.organizationId() != null) {
            return principal.organizationId();
        }

        return getCurrentUser().getOrganizationId();
    }

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

    /**
     * Returns the current authenticated user. If there is no authenticated user or the user cannot be found in the database, an AccessDeniedException is thrown.
     *
     * @return The current authenticated User entity.
     * @throws AccessDeniedException if the request is unauthenticated or the authenticated user cannot be found in the database.
     */
    public User getCurrentUser() {
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated request");
        }

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
    }

}
