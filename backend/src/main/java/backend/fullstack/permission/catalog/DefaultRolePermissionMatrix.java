package backend.fullstack.permission.catalog;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import backend.fullstack.permission.model.Permission;
import backend.fullstack.user.role.Role;

/**
 * Utility class that defines the default mapping of permissions for each role in the IK-Control system. This class provides a method to create an immutable map that associates each role with its corresponding set of permissions.
 * 
 * @version 1.0
 * @since 31.03.26
 */
public final class DefaultRolePermissionMatrix {

    private DefaultRolePermissionMatrix() {
    }

    public static Map<Role, Set<Permission>> create() {
        Map<Role, Set<Permission>> mapping = new EnumMap<>(Role.class);

        mapping.put(Role.ADMIN, EnumSet.allOf(Permission.class));

        mapping.put(Role.SUPERVISOR, EnumSet.of(
                Permission.USERS_READ_ORGANIZATION,
                Permission.USERS_CREATE,
                Permission.USERS_UPDATE,
                Permission.USERS_DEACTIVATE,
                Permission.USERS_ASSIGN_LOCATIONS,
                Permission.ORGANIZATION_SETTINGS_READ,
                Permission.LOCATIONS_READ,
                Permission.LOGS_TEMPERATURE_READ,
                Permission.CHECKLISTS_READ,
                Permission.DEVIATIONS_READ,
                Permission.DEVIATIONS_RESOLVE,
                Permission.REPORTS_READ,
                Permission.REPORTS_EXPORT
        ));

        mapping.put(Role.MANAGER, EnumSet.of(
                Permission.USERS_READ_LOCATION,
                Permission.USERS_CREATE,
                Permission.USERS_UPDATE,
                Permission.LOCATIONS_READ,
                Permission.LOGS_TEMPERATURE_READ,
                Permission.LOGS_TEMPERATURE_CREATE,
                Permission.CHECKLISTS_READ,
                Permission.CHECKLISTS_COMPLETE,
                Permission.CHECKLISTS_APPROVE,
                Permission.CHECKLISTS_TEMPLATE_MANAGE,
                Permission.DEVIATIONS_READ,
                Permission.DEVIATIONS_CREATE,
                Permission.DEVIATIONS_RESOLVE,
                Permission.REPORTS_READ,
                Permission.REPORTS_EXPORT
        ));

        mapping.put(Role.STAFF, EnumSet.of(
                Permission.LOGS_TEMPERATURE_CREATE,
                Permission.CHECKLISTS_COMPLETE,
                Permission.DEVIATIONS_CREATE
        ));

        return Collections.unmodifiableMap(mapping);
    }
}
