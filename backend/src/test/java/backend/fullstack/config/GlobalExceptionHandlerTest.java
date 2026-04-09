package backend.fullstack.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import backend.fullstack.dto.ErrorResponse;
import backend.fullstack.exceptions.AccessDeniedException;
import backend.fullstack.exceptions.InvalidThresholdException;
import backend.fullstack.exceptions.LocationException;
import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.exceptions.PasswordException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.exceptions.UserConflictException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void appExceptionHandlerUsesExceptionMetadata() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/meta");

        ResponseEntity<ErrorResponse> response = handler.handleAppException(new PasswordException("bad pwd"), request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("PASSWORD_ERROR", response.getBody().errorCode());
        assertEquals("bad pwd", response.getBody().message());
    }

    @Test
    void appExceptionDerivedHandlersBuildExpectedResponses() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        assertError(handler.handleResourceNotFound(new ResourceNotFoundException("missing"), request), 404, "RESOURCE_NOT_FOUND");
        assertError(handler.handleUnitNotFound(new UnitNotFoundException(7L), request), 404, "UNIT_NOT_FOUND");
        assertError(handler.handleInvalidThreshold(new InvalidThresholdException(), request), 400, "INVALID_THRESHOLD");
        assertError(handler.handleUnitInactive(new UnitInactiveException(8L), request), 409, "UNIT_INACTIVE");
    }

    @Test
    void conflictAndBadRequestHandlersBuildExpectedResponses() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        assertError(handler.handleOrganizationConflict(new OrganizationConflictException("org exists"), request), 409, "CONFLICT");
        assertError(handler.handleUserConflict(new UserConflictException("dup"), request), 409, "CONFLICT");
        assertError(handler.handleLocationException(new LocationException("loc err"), request), 409, "CONFLICT");
        assertError(handler.handleRoleException(new RoleException("role err"), request), 400, "ROLE_ERROR");
        assertError(handler.handlePasswordException(new PasswordException("pwd err"), request), 400, "PASSWORD_ERROR");
        assertError(handler.handleIllegalArgument(new IllegalArgumentException("bad arg"), request), 400, "BAD_REQUEST");
    }

    @Test
    void accessDeniedHandlersBuildExpectedResponses() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");

        assertError(handler.handleCustomAccessDeniedException(new AccessDeniedException("denied"), request), 403, "ACCESS_DENIED");
        assertError(handler.handleSpringAccessDeniedException(new org.springframework.security.access.AccessDeniedException("denied"), request), 403, "ACCESS_DENIED");
    }

    @Test
    void validationHandlerCollectsFirstFieldErrorPerField() throws Exception {
        Object target = new ValidationTarget();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "validationTarget");
        List<FieldError> fieldErrors = List.of(
                new FieldError("obj", "email", "must be valid"),
                new FieldError("obj", "email", "duplicate should be ignored"),
                new FieldError("obj", "name", "must not be blank")
        );
        fieldErrors.forEach(bindingResult::addError);

        Method method = ValidationTarget.class.getDeclaredMethod("submit", ValidationTarget.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().errorCode());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals(Map.of("email", "must be valid", "name", "must not be blank"), response.getBody().fieldErrors());
    }

    private static void assertError(ResponseEntity<ErrorResponse> response, int status, String code) {
        assertEquals(status, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(code, response.getBody().errorCode());
    }

    private static final class ValidationTarget {
        @SuppressWarnings("unused")
        void submit(ValidationTarget value) {
        }
    }
}
