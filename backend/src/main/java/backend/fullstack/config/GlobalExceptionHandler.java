package backend.fullstack.config;


import backend.fullstack.dto.ErrorResponse;
import backend.fullstack.exceptions.AppException;
import backend.fullstack.exceptions.OrganizationConflictException;
import backend.fullstack.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

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

    @ExceptionHandler(OrganizationConflictException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationConflict(OrganizationConflictException ex, HttpServletRequest request) {
        logger.warn("Organization conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
    }

    /**
     * Helper method to build error response from AppException.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(AppException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    /**
     * Helper method to build error response with specified parameters.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
