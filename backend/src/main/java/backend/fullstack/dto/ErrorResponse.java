package backend.fullstack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response structure for API error handling.
 * Includes timestamp, HTTP status, error message, error code, and request path.
 * Designed to be used in global exception handling to provide consistent error responses across the API.
 *
 * @version 1.0
 * @since 27.03.26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        Map<String, String> fieldErrors,
        String path
) {}
