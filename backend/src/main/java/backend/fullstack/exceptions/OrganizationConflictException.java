package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an organization operation violates a uniqueness or business constraint.
 *
 * @version 1.0
 * @since 31.03.26
 */
public class OrganizationConflictException extends AppException {

    /**
     * Constructs a new OrganizationConflictException with the specified detail message.
     *
     * @param message the detail message
     */
    public OrganizationConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "ORGANIZATION_CONFLICT");
    }
}