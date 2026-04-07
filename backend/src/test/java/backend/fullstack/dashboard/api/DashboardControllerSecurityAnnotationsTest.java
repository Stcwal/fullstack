package backend.fullstack.dashboard.api;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class DashboardControllerSecurityAnnotationsTest {

    @Test
    void getDashboardAllowsAllAuthenticatedRoles() {
        assertPreAuthorize("getDashboard", "hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')");
    }

    private void assertPreAuthorize(String methodName, String expectedValue) {
        Method method = Arrays.stream(DashboardController.class.getDeclaredMethods())
                .filter(candidate -> methodName.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found: " + methodName));

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize must be present on " + methodName);
        assertEquals(expectedValue, annotation.value(), "Unexpected role guard on " + methodName);
    }
}
