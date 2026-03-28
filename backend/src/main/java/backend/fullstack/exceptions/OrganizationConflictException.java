package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

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