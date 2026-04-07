package backend.fullstack.permission.catalog;

import java.util.EnumSet;
import java.util.EnumMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import backend.fullstack.permission.definition.PermissionDefinition;
import backend.fullstack.permission.model.Permission;
import backend.fullstack.permission.model.PermissionConditionType;
import backend.fullstack.permission.model.PermissionEffect;
import backend.fullstack.permission.model.PermissionScope;
import backend.fullstack.permission.override.UserPermissionOverride;
import backend.fullstack.permission.override.UserPermissionOverrideRepository;
import backend.fullstack.permission.profile.PermissionProfile;
import backend.fullstack.permission.profile.PermissionProfileBinding;
import backend.fullstack.permission.profile.PermissionProfileBindingRepository;
import backend.fullstack.permission.profile.UserProfileAssignment;
import backend.fullstack.permission.profile.UserProfileAssignmentRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserLocationScopeAssignmentRepository;
import backend.fullstack.user.role.Role;

/**
 * Default role-permission mapping.
 *
 * Keep this catalog as a single source of truth until role/permission mappings
 * are moved to database-backed configuration.
 * 
 * @version 1.0
 * @since 30.03.26
 */
@Component
public class RolePermissionCatalog {

    private static final Logger logger = LoggerFactory.getLogger(RolePermissionCatalog.class);

    private final RolePermissionBindingRepository rolePermissionBindingRepository;
    private final UserProfileAssignmentRepository userProfileAssignmentRepository;
    private final PermissionProfileBindingRepository permissionProfileBindingRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;
    private final UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository;
    private final Map<Role, Set<Permission>> fallbackPermissions;

    public RolePermissionCatalog(
            RolePermissionBindingRepository rolePermissionBindingRepository,
            UserProfileAssignmentRepository userProfileAssignmentRepository,
            PermissionProfileBindingRepository permissionProfileBindingRepository,
            UserPermissionOverrideRepository userPermissionOverrideRepository,
            UserLocationScopeAssignmentRepository userLocationScopeAssignmentRepository
    ) {
        this.rolePermissionBindingRepository = rolePermissionBindingRepository;
        this.userProfileAssignmentRepository = userProfileAssignmentRepository;
        this.permissionProfileBindingRepository = permissionProfileBindingRepository;
        this.userPermissionOverrideRepository = userPermissionOverrideRepository;
        this.userLocationScopeAssignmentRepository = userLocationScopeAssignmentRepository;
        this.fallbackPermissions = new EnumMap<>(DefaultRolePermissionMatrix.create());
    }

    /**
     * Returns the set of permissions associated with a given role.
     * 
     * @param role The role for which to retrieve permissions.
     * @return A set of permissions associated with the role. Returns an empty set if the
     * role is null or if there are no permissions defined for the role.
     * @throws RuntimeException if there is an error accessing the database. In this case, a fallback permission set will be returned based on the default role-permission matrix.
     */
    public Set<Permission> getPermissions(Role role) {
        if (role == null) {
            return Set.of();
        }

        try {
            if (!rolePermissionBindingRepository.existsByRole(role)) {
                return copyFallback(role);
            }

            List<RolePermissionBinding> bindings = rolePermissionBindingRepository.findByRole(role);
            Set<Permission> dbPermissions = bindings.stream()
                    .map(RolePermissionBinding::getPermission)
                    .map(PermissionDefinition::getPermissionKey)
                    .map(Permission::fromKey)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));

            return dbPermissions.isEmpty() ? copyFallback(role) : dbPermissions;
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve role permissions from DB for role {}. Using fallback mapping.", role, ex);
            return copyFallback(role);
        }
    }

    /**
     * Resolves effective grants for a specific user.
     *
     * Composition order:
     * - role defaults/bindings
     * - profile grants
     * - user explicit grants
     * - user explicit denies removed last
     * 
     * @param user The user for which to resolve permissions.
     * @return A set of effective permissions for the user. Returns an empty set if the user is null or if there are no permissions defined for the user.
     */
    public Set<Permission> getEffectivePermissions(User user) {
        return getEffectivePermissions(user, null);
    }

    public Set<Permission> getEffectivePermissions(User user, Long locationId) {
        if (user == null || user.getRole() == null) {
            return Set.of();
        }

        Set<Permission> effective = EnumSet.noneOf(Permission.class);

        // When a user is actively acting at a location, the location-level profile set is the
        // primary permission source for that location. This allows temporary downscoping (for
        // example a restaurant manager covering a shift leader shift elsewhere).
        boolean actingAtLocation = isActingAtLocation(user, locationId);
        if (!actingAtLocation) {
            effective.addAll(getPermissions(user.getRole()));
        }

        effective.addAll(resolveProfilePermissions(user, locationId));
        effective.addAll(resolveUserGrantedPermissions(user, locationId));
        effective.removeAll(resolveUserDeniedPermissions(user, locationId));
        return effective;
    }

    /**
     * Returns explicit deny overrides for the user.
     *
     * This is kept separate so AuthorizationService can enforce deny > grant precedence.
     * 
     * @param user The user for which to retrieve denied permissions.
     * @return A set of permissions that are explicitly denied for the user. Returns an empty
     */
    public Set<Permission> getDeniedPermissions(User user) {
        return getDeniedPermissions(user, null);
    }

    public Set<Permission> getDeniedPermissions(User user, Long locationId) {
        return resolveUserDeniedPermissions(user, locationId);
    }

    public List<String> getActiveProfileNames(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            return userProfileAssignmentRepository.findActiveByUserId(user.getId(), now)
                    .stream()
                    .map(UserProfileAssignment::getProfile)
                    .filter(PermissionProfile::isActive)
                    .map(PermissionProfile::getName)
                    .distinct()
                    .sorted()
                    .toList();
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve active profile names for user {}", user.getId(), ex);
            return List.of();
        }
    }

    /**
     * Resolves permissions granted through active permission profiles assigned to the user.
     * 
     * 
     * @param user The user for which to resolve profile permissions.
     * @return A set of permissions granted to the user through their active permission profiles. Returns
     */
    private Set<Permission> resolveProfilePermissions(User user, Long locationId) {
        if (user == null || user.getId() == null) {
            return EnumSet.noneOf(Permission.class);
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            boolean actingAtLocation = isActingAtLocation(user, locationId);

            List<Long> profileIds = userProfileAssignmentRepository.findActiveByUserId(user.getId(), now)
                    .stream()
                    .filter(assignment -> isAssignmentApplicable(assignment, locationId, actingAtLocation))
                    .map(UserProfileAssignment::getProfile)
                    .filter(PermissionProfile::isActive)
                    .map(PermissionProfile::getId)
                    .toList();

            if (profileIds.isEmpty()) {
                return EnumSet.noneOf(Permission.class);
            }

            return permissionProfileBindingRepository.findByProfile_IdIn(profileIds)
                    .stream()
                    .filter(binding -> isBindingApplicable(binding, locationId))
                    .map(PermissionProfileBinding::getPermission)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve profile permissions for user {}", user.getId(), ex);
            return EnumSet.noneOf(Permission.class);
        }
    }

    /**
     * Resolves permissions granted to the user directly, bypassing profile assignments.
     *
     * @param user The user for which to resolve granted permissions.
     * @return A set of permissions explicitly granted to the user. Returns an empty set if none are found.
     */
    private Set<Permission> resolveUserGrantedPermissions(User user, Long locationId) {
        if (user == null || user.getId() == null) {
            return EnumSet.noneOf(Permission.class);
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            return userPermissionOverrideRepository.findActiveByUserId(user.getId(), now)
                    .stream()
                    .filter(override -> override.getEffect() == PermissionEffect.ALLOW)
                    .filter(override -> isOverrideApplicable(override, locationId))
                    .map(UserPermissionOverride::getPermission)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve user grants for user {}", user.getId(), ex);
            return EnumSet.noneOf(Permission.class);
        }
    }

    /**
     * Resolves permissions explicitly denied to the user, which take precedence over grants.
     *
     * @param user The user for which to resolve denied permissions.
     * @return A set of permissions explicitly denied to the user. Returns an empty set if none are found.
     */
    private Set<Permission> resolveUserDeniedPermissions(User user, Long locationId) {
        if (user == null || user.getId() == null) {
            return EnumSet.noneOf(Permission.class);
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            return userPermissionOverrideRepository.findActiveByUserId(user.getId(), now)
                    .stream()
                    .filter(override -> override.getEffect() == PermissionEffect.DENY)
                    .filter(override -> isOverrideApplicable(override, locationId))
                    .map(UserPermissionOverride::getPermission)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve user denies for user {}", user.getId(), ex);
            return EnumSet.noneOf(Permission.class);
        }
    }

    /**
     * Creates a copy of the fallback permissions for the specified role to ensure immutability.
     *
     * @param role The role for which to copy fallback permissions.
     * @return A new set containing the fallback permissions for the role. Returns an empty set if the role is null or if there are no fallback permissions defined for the role.
     */
    private Set<Permission> copyFallback(Role role) {
        Set<Permission> permissions = fallbackPermissions.get(role);
        return permissions == null ? Set.of() : EnumSet.copyOf(permissions);
    }

    private boolean isBindingApplicable(PermissionProfileBinding binding, Long locationId) {
        if (binding.getScope() == PermissionScope.ORGANIZATION) {
            return true;
        }

        if (binding.getScope() == PermissionScope.LOCATION) {
            return locationId != null && locationId.equals(binding.getLocationId());
        }

        return false;
    }

    private boolean isOverrideApplicable(UserPermissionOverride override, Long locationId) {
        if (override.getScope() == PermissionScope.ORGANIZATION) {
            return true;
        }

        if (override.getScope() == PermissionScope.LOCATION) {
            return locationId != null && locationId.equals(override.getLocationId());
        }

        return false;
    }

    private boolean isActingAtLocation(User user, Long locationId) {
        if (user == null || user.getId() == null || locationId == null) {
            return false;
        }

        try {
            return userLocationScopeAssignmentRepository
                    .existsActiveActingAssignment(user.getId(), locationId, LocalDateTime.now());
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve acting assignment for user {} at location {}", user.getId(), locationId, ex);
            return false;
        }
    }

    private boolean isAssignmentApplicable(UserProfileAssignment assignment, Long locationId, boolean actingAtLocation) {
        Long assignmentLocationId = assignment.getLocation() == null ? null : assignment.getLocation().getId();

        if (actingAtLocation && locationId != null) {
            return locationId.equals(assignmentLocationId);
        }

        if (locationId == null) {
            return assignmentLocationId == null;
        }

        return assignmentLocationId == null || locationId.equals(assignmentLocationId);
    }
}
