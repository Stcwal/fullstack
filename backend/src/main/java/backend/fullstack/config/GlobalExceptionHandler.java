package backend.fullstack.config;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import backend.fullstack.dto.ErrorResponse;
import backend.fullstack.exceptions.AppException;
import backend.fullstack.exceptions.InvalidThresholdException;
import backend.fullstack.exceptions.LocationException;
import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.exceptions.PasswordException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import backend.fullstack.exceptions.RoleException;
import backend.fullstack.exceptions.UnitInactiveException;
import backend.fullstack.exceptions.UnitNotFoundException;
import backend.fullstack.exceptions.UserConflictException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler that can be extended to handle specific exceptions across all controllers.
 *
 * @version 1.0
 * @since 27.03.26
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle custom AppException and its subclasses.
     * Extracts HTTP status and error code from the exception.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        logger.warn("Application exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnitNotFound(UnitNotFoundException ex, HttpServletRequest request) {
        logger.warn("Unit not found: {}", ex.getMessage());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(InvalidThresholdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidThreshold(
            InvalidThresholdException ex, HttpServletRequest request) {
        logger.warn("Invalid threshold values: {}", ex.getMessage());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(UnitInactiveException.class)
    public ResponseEntity<ErrorResponse> handleUnitInactive(UnitInactiveException ex, HttpServletRequest request) {
        logger.warn("Inactive unit usage blocked: {}", ex.getMessage());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(OrganizationConflictException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationConflict(OrganizationConflictException ex, HttpServletRequest request) {
        logger.warn("Organization conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(UserConflictException.class)
    public ResponseEntity<ErrorResponse> handleUserConflict(UserConflictException ex, HttpServletRequest request) {
        logger.warn("User conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(LocationException.class)
    public ResponseEntity<ErrorResponse> handleLocationException(LocationException ex, HttpServletRequest request) {
        logger.warn("Location error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(RoleException.class)
    public ResponseEntity<ErrorResponse> handleRoleException(RoleException ex, HttpServletRequest request) {
        logger.warn("Role error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ROLE_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<ErrorResponse> handlePasswordException(PasswordException ex, HttpServletRequest request) {
        logger.warn("Password error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "PASSWORD_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(backend.fullstack.exceptions.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleCustomAccessDeniedException(backend.fullstack.exceptions.AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Spring access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            if (!fieldErrors.containsKey(fieldError.getField())) {
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    /**
     * Helper method to build error response from AppException.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(AppException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request, null);
    }

    /**
     * Helper method to build error response with specified parameters.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        return buildErrorResponse(status, errorCode, message, request, null);
        }

        private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
        ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
            fieldErrors,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
