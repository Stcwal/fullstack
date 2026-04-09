package backend.fullstack.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ExceptionsTest {

    @Test
    void appExceptionStoresStatusAndErrorCodeWithoutCause() {
        TestAppException exception = new TestAppException("boom", HttpStatus.BAD_REQUEST, "TEST_ERROR");

        assertEquals("boom", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void appExceptionStoresStatusAndErrorCodeWithCause() {
        RuntimeException cause = new RuntimeException("root");
        TestAppException exception = new TestAppException("boom", cause, HttpStatus.CONFLICT, "TEST_ERROR");

        assertEquals("boom", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("TEST_ERROR", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void accessDeniedExceptionSingleArgumentConstructor() {
        AccessDeniedException exception = new AccessDeniedException("forbidden");

        assertEquals("forbidden", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void accessDeniedExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        AccessDeniedException exception = new AccessDeniedException("forbidden", cause);

        assertEquals("forbidden", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("ACCESS_DENIED", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void invalidPasswordExceptionSetsUnauthorizedMetadata() {
        InvalidPasswordException exception = new InvalidPasswordException("invalid credentials");

        assertEquals("invalid credentials", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("INVALID_PASSWORD", exception.getErrorCode());
    }

    @Test
    void invalidThresholdExceptionUsesDefaultMessageAndMetadata() {
        InvalidThresholdException exception = new InvalidThresholdException();

        assertEquals("Invalid thresholds: expected minThreshold < targetTemperature < maxThreshold", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("INVALID_THRESHOLD", exception.getErrorCode());
    }

    @Test
    void locationExceptionSingleArgumentConstructor() {
        LocationException exception = new LocationException("location conflict");

        assertEquals("location conflict", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("LOCATION_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void locationExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        LocationException exception = new LocationException("location conflict", cause);

        assertEquals("location conflict", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("LOCATION_ERROR", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void organizationConflictExceptionSetsConflictMetadata() {
        OrganizationConflictException exception = new OrganizationConflictException("organization exists");

        assertEquals("organization exists", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("ORGANIZATION_CONFLICT", exception.getErrorCode());
    }

    @Test
    void passwordExceptionSingleArgumentConstructor() {
        PasswordException exception = new PasswordException("bad password");

        assertEquals("bad password", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("PASSWORD_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void passwordExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        PasswordException exception = new PasswordException("bad password", cause);

        assertEquals("bad password", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("PASSWORD_ERROR", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void resourceNotFoundExceptionMessageConstructor() {
        ResourceNotFoundException exception = new ResourceNotFoundException("not found");

        assertEquals("not found", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void resourceNotFoundExceptionResourceTypeAndIdConstructor() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", 42L);

        assertEquals("User not found with ID: 42", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void resourceNotFoundExceptionResourceTypeAndIdentifierConstructor() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "alice@example.com");

        assertEquals("User not found: alice@example.com", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void resourceNotFoundExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        ResourceNotFoundException exception = new ResourceNotFoundException("missing", cause);

        assertEquals("missing", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void roleExceptionSingleArgumentConstructor() {
        RoleException exception = new RoleException("invalid role");

        assertEquals("invalid role", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("ROLE_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void roleExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        RoleException exception = new RoleException("invalid role", cause);

        assertEquals("invalid role", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("ROLE_ERROR", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    @Test
    void unitInactiveExceptionFormatsMessageAndMetadata() {
        UnitInactiveException exception = new UnitInactiveException(7L);

        assertEquals("Temperature unit is inactive: 7", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("UNIT_INACTIVE", exception.getErrorCode());
    }

    @Test
    void unitNotFoundExceptionFormatsMessageAndMetadata() {
        UnitNotFoundException exception = new UnitNotFoundException(9L);

        assertEquals("Temperature unit not found with id: 9", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("UNIT_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void userConflictExceptionSingleArgumentConstructor() {
        UserConflictException exception = new UserConflictException("duplicate user");

        assertEquals("duplicate user", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("USER_CONFLICT", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void userConflictExceptionMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException("root");
        UserConflictException exception = new UserConflictException("duplicate user", cause);

        assertEquals("duplicate user", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("USER_CONFLICT", exception.getErrorCode());
        assertSame(cause, exception.getCause());
    }

    private static final class TestAppException extends AppException {

        private TestAppException(String message, HttpStatus httpStatus, String errorCode) {
            super(message, httpStatus, errorCode);
        }

        private TestAppException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
            super(message, cause, httpStatus, errorCode);
        }
    }
}
