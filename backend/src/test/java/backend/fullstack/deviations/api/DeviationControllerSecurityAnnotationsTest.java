package backend.fullstack.deviations.api;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class DeviationControllerSecurityAnnotationsTest {

    @Test
    void getDeviationsAllowsAllAuthenticatedRoles() {
        assertPreAuthorize("getDeviations", "hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')");
    }

    @Test
    void createDeviationAllowsAllAuthenticatedRoles() {
        assertPreAuthorize("createDeviation", "hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')");
    }

    @Test
    void resolveDeviationRequiresAdminManagerOrSupervisor() {
        assertPreAuthorize("resolveDeviation", "hasAnyRole('ADMIN','MANAGER','SUPERVISOR')");
    }

    private void assertPreAuthorize(String methodName, String expectedValue) {
        Method method = Arrays.stream(DeviationController.class.getDeclaredMethods())
                .filter(candidate -> methodName.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found: " + methodName));

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize must be present on " + methodName);
        assertEquals(expectedValue, annotation.value(), "Unexpected role guard on " + methodName);
    }
}
