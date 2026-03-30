package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error related to role operations, such as invalid role assignment or role not found.
 *
 * @version 1.0
 * @since 30.03.26
 */
public class RoleException extends AppException {


    /**
     * Constructs a new RoleException with the specified detail message.
     *
     * @param message the detail message
     */
    public RoleException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "ROLE_ERROR");
    }

    /**
     * Constructs a new RoleException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public RoleException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "ROLE_ERROR");
    }
}
