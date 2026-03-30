package backend.fullstack.permission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import backend.fullstack.exceptions.AccessDeniedException;
import backend.fullstack.permission.dto.CapabilitiesResponse;
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

    public AuthorizationService(
            AccessContextService accessContext,
            RolePermissionCatalog rolePermissionCatalog,
            UserRepository userRepository
    ) {
        this.accessContext = accessContext;
        this.rolePermissionCatalog = rolePermissionCatalog;
        this.userRepository = userRepository;
    }

    public void assertPermission(Permission permission) {
        if (!hasPermission(permission)) {
            throw new AccessDeniedException("Missing permission: " + permission.key());
        }
    }

    public boolean hasPermission(Permission permission) {
        Set<Permission> permissions = rolePermissionCatalog.getPermissions(accessContext.getCurrentRole());
        return permissions.contains(permission);
    }

    public void assertPermissionForLocation(Permission permission, Long locationId) {
        assertPermission(permission);
        accessContext.assertCanAccess(locationId);
    }

    public void assertCanViewUser(User targetUser) {
        assertPermission(Permission.USERS_READ);

        User actor = accessContext.getCurrentUser();
        assertSameOrganization(actor, targetUser);

        if (actor.getRole() == Role.ADMIN || actor.getId().equals(targetUser.getId())) {
            return;
        }

        if (!hasLocationIntersection(actor, targetUser)) {
            throw new AccessDeniedException("No access to this user");
        }
    }

    public boolean canViewUser(User targetUser) {
        try {
            assertCanViewUser(targetUser);
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

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

    public void assertCanDeactivateUser(User targetUser) {
        assertPermission(Permission.USERS_DEACTIVATE);
        assertCanManageUser(targetUser);
    }

    public CapabilitiesResponse getCurrentCapabilities() {
        Role currentRole = accessContext.getCurrentRole();
        List<Long> allowedLocationIds = accessContext.getAllowedLocationIds();

        CapabilitiesResponse response = new CapabilitiesResponse();
        response.setRole(currentRole);
        response.setOrganizationId(accessContext.getCurrentOrganizationId());
        response.setAllowedLocationIds(allowedLocationIds);
        response.setPermissions(
                rolePermissionCatalog.getPermissions(currentRole)
                        .stream()
                        .map(Permission::key)
                        .sorted()
                        .toList()
        );
        response.setManageableRoles(getManageableRoles(currentRole));
        response.setManageableLocationIds(new ArrayList<>(allowedLocationIds));

        return response;
    }

    private List<Role> getManageableRoles(Role actorRole) {
        return switch (actorRole) {
            case ADMIN -> List.of(Role.ADMIN, Role.SUPERVISOR, Role.MANAGER, Role.STAFF);
            case SUPERVISOR -> List.of(Role.MANAGER, Role.STAFF);
            case MANAGER -> List.of(Role.STAFF);
            case STAFF -> List.of();
        };
    }

    private void assertSameOrganization(User actor, User targetUser) {
        if (!actor.getOrganizationId().equals(targetUser.getOrganizationId())) {
            throw new AccessDeniedException("Cross-organization access is not allowed");
        }
    }

    private boolean hasLocationIntersection(User actor, User targetUser) {
        Set<Long> actorLocations = getUserLocationScope(actor);
        if (actorLocations.isEmpty()) {
            actorLocations = new HashSet<>(accessContext.getAllowedLocationIds());
        }

        Set<Long> targetLocations = getUserLocationScope(targetUser);
        targetLocations.retainAll(actorLocations);
        return !targetLocations.isEmpty();
    }

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
}