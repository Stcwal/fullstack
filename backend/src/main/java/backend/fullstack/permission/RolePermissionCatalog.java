package backend.fullstack.permission;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

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

    private final Map<Role, Set<Permission>> rolePermissions;

    public RolePermissionCatalog() {
        Map<Role, Set<Permission>> mapping = new EnumMap<>(Role.class);

        mapping.put(Role.ADMIN, EnumSet.allOf(Permission.class));

        mapping.put(Role.SUPERVISOR, EnumSet.of(
                Permission.USERS_READ,
                Permission.USERS_CREATE,
                Permission.USERS_UPDATE,
                Permission.USERS_DEACTIVATE,
                Permission.USERS_ASSIGN_LOCATIONS,
                Permission.ORGANIZATION_SETTINGS_READ,
                Permission.LOCATIONS_READ,
                Permission.TEMPERATURE_READ,
                Permission.CHECKLIST_READ,
                Permission.DEVIATIONS_READ,
                Permission.DEVIATIONS_RESOLVE,
                Permission.REPORTS_READ,
                Permission.REPORTS_EXPORT
        ));

        mapping.put(Role.MANAGER, EnumSet.of(
                Permission.USERS_READ,
                Permission.USERS_CREATE,
                Permission.USERS_UPDATE,
                Permission.LOCATIONS_READ,
                Permission.TEMPERATURE_READ,
                Permission.TEMPERATURE_LOG,
                Permission.CHECKLIST_READ,
                Permission.CHECKLIST_COMPLETE,
                Permission.CHECKLIST_TEMPLATE_MANAGE,
                Permission.DEVIATIONS_READ,
                Permission.DEVIATIONS_CREATE,
                Permission.DEVIATIONS_RESOLVE,
                Permission.REPORTS_READ,
                Permission.REPORTS_EXPORT
        ));

        mapping.put(Role.STAFF, EnumSet.of(
                Permission.TEMPERATURE_READ,
                Permission.TEMPERATURE_LOG,
                Permission.CHECKLIST_READ,
                Permission.CHECKLIST_COMPLETE,
                Permission.DEVIATIONS_READ,
                Permission.DEVIATIONS_CREATE
        ));

        this.rolePermissions = Collections.unmodifiableMap(mapping);
    }

    public Set<Permission> getPermissions(Role role) {
        if (role == null) {
            return Set.of();
        }

        Set<Permission> permissions = rolePermissions.get(role);
        return permissions == null ? Set.of() : EnumSet.copyOf(permissions);
    }
}