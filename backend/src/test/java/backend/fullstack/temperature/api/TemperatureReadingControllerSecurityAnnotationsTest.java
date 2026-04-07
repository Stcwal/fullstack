package backend.fullstack.temperature.api;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class TemperatureReadingControllerSecurityAnnotationsTest {

    @Test
    void unitReadingsAllowAllOperationalRoles() {
        assertPreAuthorize("getUnitReadings", "hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')");
    }

    @Test
    void globalReadingsRequireAdminOrManager() {
        assertPreAuthorize("getReadings", "hasAnyRole('ADMIN','MANAGER')");
    }

    @Test
    void createReadingAllowsAllOperationalRoles() {
        assertPreAuthorize("createReading", "hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')");
    }

    private void assertPreAuthorize(String methodName, String expectedValue) {
        Method method = Arrays.stream(TemperatureReadingController.class.getDeclaredMethods())
                .filter(candidate -> methodName.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found: " + methodName));

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize must be present on " + methodName);
        assertEquals(expectedValue, annotation.value(), "Unexpected role guard on " + methodName);
    }
}