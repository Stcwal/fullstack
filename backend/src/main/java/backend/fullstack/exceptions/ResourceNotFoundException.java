package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 *
 * @version 1.0
 * @since 27.03.26
 */
public class ResourceNotFoundException extends AppException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail resourceType, and ID.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Location")
     * @param id The ID of the resource that was not found.
     */
    public ResourceNotFoundException(String resourceType, Long id) {
        super(
                String.format("%s not found with ID: %d", resourceType, id),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail resourceType, and identifier.
     *
     * @param resourceType the type of resource that was not found (e.g., "User", "Location")
     * @param identifier The unique identifier of the resource that was not found (e.g., username, email).
     */
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
                String.format("%s not found: %s", resourceType, identifier),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
