package backend.fullstack.permission.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PermissionProfileTest {

    @Test
    void builderDefaultsToActive() {
        PermissionProfile profile = PermissionProfile.builder()
                .name("Shift Leader")
                .description("Can approve checklist")
                .build();

        assertTrue(profile.isActive());
    }

    @Test
    void builderAllowsExplicitInactive() {
        PermissionProfile profile = PermissionProfile.builder()
                .name("Legacy")
                .isActive(false)
                .build();

        assertFalse(profile.isActive());
    }

    @Test
    void onCreateSetsCreatedTimestamp() {
        PermissionProfile profile = PermissionProfile.builder()
                .name("Kitchen")
                .build();

        assertNull(profile.getCreatedAt());
        profile.onCreate();
        assertNotNull(profile.getCreatedAt());
    }
}
