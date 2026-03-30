package backend.fullstack.permission;

/**
 * Canonical permission keys used across backend authorization checks.
 */
public enum Permission {
    USERS_READ("users.read"),
    USERS_CREATE("users.create"),
    USERS_UPDATE("users.update"),
    USERS_DEACTIVATE("users.deactivate"),
    USERS_ASSIGN_LOCATIONS("users.assign_locations"),
    ORGANIZATION_SETTINGS_READ("organization.settings.read"),
    ORGANIZATION_SETTINGS_UPDATE("organization.settings.update"),
    LOCATIONS_READ("locations.read"),
    LOCATIONS_MANAGE("locations.manage"),
    TEMPERATURE_READ("temperature.read"),
    TEMPERATURE_LOG("temperature.log"),
    CHECKLIST_READ("checklist.read"),
    CHECKLIST_COMPLETE("checklist.complete"),
    CHECKLIST_TEMPLATE_MANAGE("checklist.template.manage"),
    DEVIATIONS_READ("deviations.read"),
    DEVIATIONS_CREATE("deviations.create"),
    DEVIATIONS_RESOLVE("deviations.resolve"),
    REPORTS_READ("reports.read"),
    REPORTS_EXPORT("reports.export");

    private final String key;

    Permission(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}