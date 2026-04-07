package backend.fullstack.permission.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Canonical permission keys used across backend authorization checks.
 * 
 * @version 1.0
 * @since 30.03.26
 */
public enum Permission {
    // Users
    USERS_READ_LOCATION("users.read.location"),
    USERS_READ_ORGANIZATION("users.read.organization"),
    USERS_CREATE("users.create"),
    USERS_UPDATE("users.update"),
    USERS_DEACTIVATE("users.deactivate"),
    USERS_ASSIGN_LOCATIONS("users.assign_locations"),

    // Organization and locations
    ORGANIZATION_SETTINGS_READ("organization.settings.read"),
    ORGANIZATION_SETTINGS_UPDATE("organization.settings.update"),
    LOCATIONS_READ("locations.read"),
    LOCATIONS_MANAGE("locations.manage"),

    // Logs
    LOGS_TEMPERATURE_READ("logs.temperature.read"),
    LOGS_TEMPERATURE_CREATE("logs.temperature.create"),
    LOGS_FREEZER_CREATE("logs.freezer.create"),

    // Checklists
    CHECKLISTS_READ("checklists.read"),
    CHECKLISTS_COMPLETE("checklists.complete"),
    CHECKLISTS_APPROVE("checklists.approve"),
    CHECKLISTS_TEMPLATE_MANAGE("checklists.template.manage"),

    // Deviations
    DEVIATIONS_READ("deviations.read"),
    DEVIATIONS_CREATE("deviations.create"),
    DEVIATIONS_RESOLVE("deviations.resolve"),

    // Reports
    REPORTS_READ("reports.read"),
    REPORTS_EXPORT("reports.export");

    private final String key;
    private static final Map<String, Permission> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Permission::key, permission -> permission));

    Permission(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    /**
     * Returns the permission corresponding to the given key.
     *
     * @param key the permission key
     * @return the corresponding permission
     * @throws IllegalArgumentException if no permission is found for the given key
     */
    public static Permission fromKey(String key) {
        Permission permission = BY_KEY.get(key);
        if (permission == null) {
            throw new IllegalArgumentException("Unknown permission key: " + key);
        }
        return permission;
    }
}
