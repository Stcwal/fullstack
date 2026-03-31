package backend.fullstack.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import backend.fullstack.exceptions.AccessDeniedException;
import backend.fullstack.permission.dto.CapabilitiesResponse;
import backend.fullstack.permission.dto.LocationCapabilitiesResponse;
import backend.fullstack.user.AccessContextService;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;
import backend.fullstack.user.role.Role;

/**
 * Centralized authorization evaluator.
 *
 * All permission checks should flow through this service to avoid role drift
 * and duplicated authorization logic across modules.
 * 
 * @version 1.0
 * @since 30.03.26
 */
@Service
public class AuthorizationService {

    private final AccessContextService accessContext;
    private final RolePermissionCatalog rolePermissionCatalog;
    private final UserRepository userRepository;
    private final ConditionEvaluator conditionEvaluator;

    public AuthorizationService(
            AccessContextService accessContext,
            RolePermissionCatalog rolePermissionCatalog,
            UserRepository userRepository,
            ConditionEvaluator conditionEvaluator
    ) {
        this.accessContext = accessContext;
        this.rolePermissionCatalog = rolePermissionCatalog;
        this.userRepository = userRepository;
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Asserts that the current user has the specified permission. If the user does not have the required permission, an AccessDeniedException is thrown.
     * 
     * @param permission the permission to check
     * @throws AccessDeniedException if the user does not have the required permission
     */
    public void assertPermission(Permission permission) {
        if (!hasPermission(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission.key());
        }
    }

    /**
     * Checks if the current user has the specified permission.
     *
     * @param permission the permission to check
     * @return true if the user has the required permission, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        User actor = accessContext.getCurrentUser();
        return hasEffectivePermission(actor, permission, null);
    }

    /**
     * Checks if the current user has the specified permission for a given location. This method first checks if the user has the effective permission, and then verifies that the location ID is within the user's allowed location scope. If the user does not have the required permission or access to the location, false is returned.
     * 
     * @param permission the permission to check
     * @param locationId the ID of the location to check access for
     * @return true if the user has the required permission and access to the location, false
     */
    public boolean hasPermissionForLocation(Permission permission, Long locationId) {
        User actor = accessContext.getCurrentUser();
        if (!hasEffectivePermission(actor, permission, locationId)) {
            return false;
        }

        if (locationId == null) {
            return false;
        }

        return accessContext.getAllowedLocationIds().contains(locationId);
    }

    /**
     * Checks if the current user has the specified permission for a given location, taking into account any additional conditions that may be associated with the permission. This method performs a series of checks including deny overrides, location scope validation, and condition evaluation to determine if the permission should be granted. If any of the checks fail, false is returned; otherwise, true is returned if the user has the effective permission.
     * 
     * @param permission the permission to check
     * @param locationId the ID of the location to check access for
     * @param conditionContext the contextual information relevant to the permission evaluation, which may include factors such as training completion status or approval requirements
     * @return true if the user has the required permission for the location and satisfies all conditions
     */
    public boolean hasPermissionWithCondition(
            Permission permission,
            Long locationId,
            PermissionConditionContext conditionContext
    ) {
        User actor = accessContext.getCurrentUser();

        // 1) deny override wins (resolved by location-aware effective permission evaluation)
        if (!hasEffectivePermission(actor, permission, locationId)) {
            return false;
        }

        // 2) scope check
        if (locationId != null && !accessContext.getAllowedLocationIds().contains(locationId)) {
            return false;
        }

        // 3) condition check
        PermissionConditionContext context = conditionContext == null
                ? PermissionConditionContext.empty()
                : conditionContext;
        if (!conditionEvaluator.isConditionSatisfied(actor, permission, context)) {
            return false;
        }

        // Final grant already verified in step 1.
        return true;
    }

    /**
     * Asserts that the current user has the specified permission for a given location.
     *
     * @param permission the permission to check
     * @param locationId the ID of the location to check access for
     * @throws AccessDeniedException if the user does not have the required permission or access to the location
     */
    public void assertPermissionForLocation(Permission permission, Long locationId) {
        if (!hasPermissionForLocation(permission, locationId)) {
            throw new AccessDeniedException("Missing location-scoped permission: " + permission.key());
        }
    }

     /**
      * Asserts that the current user has permission to view the specified target user. The method checks if the current user has either organization-level or location-level read permissions for users, and then verifies that the target user belongs to the same organization. If the current user is an ADMIN or is trying to view their own user record, access is granted. For other cases, if organization-level read permission is not present, it checks for location intersection between the current user and the target user. If any of the checks fail, an AccessDeniedException is thrown.
      * 
      * @param targetUser the user whose information is being accessed
      * @throws AccessDeniedException if the current user does not have permission to view the target
      */
    public void assertCanViewUser(User targetUser) {
        boolean hasOrgRead = hasPermission(Permission.USERS_READ_ORGANIZATION);
        boolean hasLocationRead = hasPermission(Permission.USERS_READ_LOCATION);
        if (!hasOrgRead && !hasLocationRead) {
            throw new AccessDeniedException("Missing permission: users.read.location or users.read.organization");
        }

        User actor = accessContext.getCurrentUser();
        assertSameOrganization(actor, targetUser);

        if (actor.getRole() == Role.ADMIN || actor.getId().equals(targetUser.getId())) {
            return;
        }

        if (hasOrgRead) {
            return;
        }

        if (!hasLocationIntersection(actor, targetUser)) {
            throw new AccessDeniedException("No access to this user");
        }
    }

    /**
     * Checks if the current user can view the specified target user.
     *
     * @param targetUser the user whose information is being accessed
     * @return true if the current user can view the target user, false otherwise
     */
    public boolean canViewUser(User targetUser) {
        try {
            assertCanViewUser(targetUser);
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

    /**
     * Asserts that the current user has permission to create a new user with the specified target role and primary location. The method checks if the current user has the USERS_CREATE permission, and then applies role-based constraints to determine if the creation is allowed. For example, an ADMIN can create MANAGER or STAFF users but must have access to the primary location, while a SUPERVISOR
     * can only create MANAGER or STAFF users and must have access to the primary location. A MANAGER can only create STAFF users and must have access to the primary location. If any of the checks fail, an AccessDeniedException is thrown.
     * @param targetRole the role of the user being created
     * @param primaryLocationId the primary location ID to be assigned to the new user, which is used to determine if the creator has access to that location
     * @throws AccessDeniedException if the current user does not have permission to create the user with the specified role and location
     */
    public void assertCanCreateUser(Role targetRole, Long primaryLocationId) {
        assertPermission(Permission.USERS_CREATE);

        User actor = accessContext.getCurrentUser();

        switch (actor.getRole()) {
            case ADMIN -> {
                if (targetRole == Role.MANAGER || targetRole == Role.STAFF) {
                    accessContext.assertCanAccess(primaryLocationId);
                }
            }
            case SUPERVISOR -> {
                if (targetRole != Role.MANAGER && targetRole != Role.STAFF) {
                    throw new AccessDeniedException("Supervisors can only create MANAGER or STAFF users");
                }
                accessContext.assertCanAccess(primaryLocationId);
            }
            case MANAGER -> {
                if (targetRole != Role.STAFF) {
                    throw new AccessDeniedException("Managers can only create STAFF users");
                }
                accessContext.assertCanAccess(primaryLocationId);
            }
            case STAFF -> throw new AccessDeniedException("Staff cannot create users");
        }
    }

    /**
     * Asserts that the current user has permission to manage the specified target user.
     *
     * @param targetUser the user whose information is being managed
     * @throws AccessDeniedException if the current user does not have permission to manage the target user
     */
    public void assertCanManageUser(User targetUser) {
        assertPermission(Permission.USERS_UPDATE);

        User actor = accessContext.getCurrentUser();
        assertSameOrganization(actor, targetUser);

        if (actor.getRole() == Role.ADMIN) {
            return;
        }

        if (targetUser.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can manage ADMIN users");
        }

        if (actor.getRole() == Role.SUPERVISOR) {
            if (targetUser.getRole() == Role.SUPERVISOR) {
                throw new AccessDeniedException("Supervisors cannot manage other supervisors");
            }

            if (!hasLocationIntersection(actor, targetUser)) {
                throw new AccessDeniedException("Cannot manage users outside assigned locations");
            }
            return;
        }

        if (actor.getRole() == Role.MANAGER) {
            if (targetUser.getRole() != Role.STAFF) {
                throw new AccessDeniedException("Managers can only manage STAFF users");
            }

            if (!hasLocationIntersection(actor, targetUser)) {
                throw new AccessDeniedException("Managers can only manage staff in their own location scope");
            }
            return;
        }

        throw new AccessDeniedException("Staff cannot manage users");
    }

    /**
     * Asserts that the current user has permission to change the role of the specified target user.
     *
     * @param targetUser the user whose role is being changed
     * @param newRole the new role to be assigned
     * @param primaryLocationId the primary location ID associated with the user
     * @throws AccessDeniedException if the current user does not have permission to change the user's role
     */
    public void assertCanChangeRole(User targetUser, Role newRole, Long primaryLocationId) {
        assertCanManageUser(targetUser);

        User actor = accessContext.getCurrentUser();
        if (actor.getRole() == Role.ADMIN) {
            return;
        }

        if (actor.getRole() == Role.SUPERVISOR) {
            if (newRole != Role.MANAGER && newRole != Role.STAFF) {
                throw new AccessDeniedException("Supervisors can only assign MANAGER and STAFF roles");
            }

            if (newRole == Role.MANAGER || newRole == Role.STAFF) {
                accessContext.assertCanAccess(primaryLocationId);
            }
            return;
        }

        if (actor.getRole() == Role.MANAGER) {
            if (newRole != Role.STAFF) {
                throw new AccessDeniedException("Managers can only assign STAFF role");
            }
            accessContext.assertCanAccess(primaryLocationId);
            return;
        }

        throw new AccessDeniedException("Insufficient role for role updates");
    }

    /**
     * Asserts that the current user has permission to assign locations to the specified target user.
     *
     * @param targetUser the user to whom locations are being assigned
     * @param requestedLocationIds the list of location IDs to be assigned
     * @throws AccessDeniedException if the current user does not have permission to assign the requested locations
     */
    public void assertCanAssignLocations(User targetUser, List<Long> requestedLocationIds) {
        assertPermission(Permission.USERS_ASSIGN_LOCATIONS);
        assertCanManageUser(targetUser);

        if (requestedLocationIds == null || requestedLocationIds.isEmpty()) {
            throw new AccessDeniedException("At least one location must be assigned");
        }

        User actor = accessContext.getCurrentUser();
        if (actor.getRole() == Role.ADMIN) {
            return;
        }

        Set<Long> allowed = new HashSet<>(accessContext.getAllowedLocationIds());
        boolean hasForbiddenLocation = requestedLocationIds.stream().anyMatch(locationId -> !allowed.contains(locationId));
        if (hasForbiddenLocation) {
            throw new AccessDeniedException("Cannot assign locations outside your scope");
        }
    }

    /**
     * Asserts that the current user has permission to deactivate the specified target user.
     *
     * @param targetUser the user to be deactivated
     * @throws AccessDeniedException if the current user does not have permission to deactivate the target user
     */
    public void assertCanDeactivateUser(User targetUser) {
        assertPermission(Permission.USERS_DEACTIVATE);
        assertCanManageUser(targetUser);
    }

    /**
     * Returns the capabilities of the current user based on their role and permissions.
     *
     * @return the capabilities response containing the user's role, permissions, and accessible locations
     */
    public CapabilitiesResponse getCurrentCapabilities() {
        User actor = accessContext.getCurrentUser();
        Role currentRole = accessContext.getCurrentRole();
        List<Long> allowedLocationIds = accessContext.getAllowedLocationIds();
        Map<String, List<Long>> scopeByPermission = resolvePermissionScopeMap(actor, allowedLocationIds);

        CapabilitiesResponse response = new CapabilitiesResponse();
        response.setRole(currentRole);
        response.setOrganizationId(accessContext.getCurrentOrganizationId());
        response.setAllowedLocationIds(allowedLocationIds);
        response.setPermissions(scopeByPermission.keySet().stream().sorted().toList());
        response.setManageableRoles(getManageableRoles(currentRole));
        response.setManageableLocationIds(new ArrayList<>(allowedLocationIds));
        response.setActiveProfileNames(rolePermissionCatalog.getActiveProfileNames(actor));
        response.setPermissionScopeLocationIds(scopeByPermission);
        response.setLocations(resolveLocationCapabilities(actor, allowedLocationIds));

        return response;
    }

    /**
     * Returns the list of roles that the current user can manage based on their role.
     *
     * @param actorRole the role of the current user
     * @return the list of manageable roles
     */
    private List<Role> getManageableRoles(Role actorRole) {
        return switch (actorRole) {
            case ADMIN -> List.of(Role.ADMIN, Role.SUPERVISOR, Role.MANAGER, Role.STAFF);
            case SUPERVISOR -> List.of(Role.MANAGER, Role.STAFF);
            case MANAGER -> List.of(Role.STAFF);
            case STAFF -> List.of();
        };
    }

    /**
     * Asserts that the current user and the target user belong to the same organization.
     *
     * @param actor the current user
     * @param targetUser the target user
     * @throws AccessDeniedException if the users belong to different organizations
     */
    private void assertSameOrganization(User actor, User targetUser) {
        if (!actor.getOrganizationId().equals(targetUser.getOrganizationId())) {
            throw new AccessDeniedException("Cross-organization access is not allowed");
        }
    }

    /**
     * Checks if the current user and the target user have any overlapping locations.
     *
     * @param actor the current user
     * @param targetUser the target user
     * @return true if there is an intersection of locations, false otherwise
     */
    private boolean hasLocationIntersection(User actor, User targetUser) {
        Set<Long> actorLocations = getUserLocationScope(actor);
        if (actorLocations.isEmpty()) {
            actorLocations = new HashSet<>(accessContext.getAllowedLocationIds());
        }

        Set<Long> targetLocations = getUserLocationScope(targetUser);
        targetLocations.retainAll(actorLocations);
        return !targetLocations.isEmpty();
    }

    /**
     * Returns the set of location IDs that the specified user has access to.
     *
     * @param user the user for whom to retrieve location scope
     * @return the set of accessible location IDs
     */
    private Set<Long> getUserLocationScope(User user) {
        Set<Long> locationIds = new HashSet<>();

        if (user.getHomeLocationId() != null) {
            locationIds.add(user.getHomeLocationId());
        }

        if (user.getId() != null) {
            locationIds.addAll(userRepository.findAdditionalLocationIdsByUserId(user.getId()));
        }

        return locationIds;
    }

    /**
     * Checks if the current user has the specified permission.
     *
     * @param actor the current user
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    private boolean hasEffectivePermission(User actor, Permission permission, Long locationId) {
        Set<Permission> effective = rolePermissionCatalog.getEffectivePermissions(actor, locationId);
        return effective.contains(permission);
    }

    /**
     * Resolves the scope map for the given user and allowed location IDs.
     *
     * @param actor the current user
     * @param allowedLocationIds the list of allowed location IDs
     * @return the map of permission scopes
     */
    private Map<String, List<Long>> resolvePermissionScopeMap(User actor, List<Long> allowedLocationIds) {
        Map<String, List<Long>> scopeByPermission = new LinkedHashMap<>();
        Set<Permission> orgWidePermissions = rolePermissionCatalog.getEffectivePermissions(actor, null);
        for (Permission permission : orgWidePermissions) {
            scopeByPermission.put(permission.key(), new ArrayList<>(allowedLocationIds));
        }

        for (Long locationId : allowedLocationIds) {
            Set<Permission> locationPermissions = rolePermissionCatalog.getEffectivePermissions(actor, locationId);
            for (Permission permission : locationPermissions) {
                scopeByPermission.computeIfAbsent(permission.key(), key -> new ArrayList<>()).add(locationId);
            }
        }

        scopeByPermission.replaceAll((key, ids) -> ids.stream().distinct().sorted().toList());

        return scopeByPermission;
    }

    private List<LocationCapabilitiesResponse> resolveLocationCapabilities(User actor, List<Long> allowedLocationIds) {
        List<LocationCapabilitiesResponse> locations = new ArrayList<>();

        for (Long locationId : allowedLocationIds) {
            Set<Permission> effective = rolePermissionCatalog.getEffectivePermissions(actor, locationId);

            LocationCapabilitiesResponse locationResponse = new LocationCapabilitiesResponse();
            locationResponse.setLocationId(locationId);
            locationResponse.setPermissions(effective.stream().map(Permission::key).sorted().toList());
            locations.add(locationResponse);
        }

        return locations;
    }
}