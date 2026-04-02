package backend.fullstack.checklist.api;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ChecklistControllerSecurityAnnotationsTest {

    @Test
    void templateWriteEndpointsRequireAdminRole() throws Exception {
        assertAdminPreAuthorize("createTemplate");
        assertAdminPreAuthorize("updateTemplate");
        assertAdminPreAuthorize("deleteTemplate");
    }

    private void assertAdminPreAuthorize(String methodName) {
        Method method = java.util.Arrays.stream(ChecklistController.class.getDeclaredMethods())
                .filter(candidate -> methodName.equals(candidate.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Method not found: " + methodName));
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertNotNull(annotation, "@PreAuthorize must be present on " + methodName);
        assertEquals("hasRole('ADMIN')", annotation.value(), "Expected ADMIN role guard on " + methodName);
    }
}
