package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is a conflict related to user operations, such as duplicate username or email.
 *
 * @version 1.0
 * @since 30.03.26
 */
public class UserConflictException  extends AppException  {

    /**
     * Constructs a new UserConflictException with the specified detail message.
     *
     * @param message the detail message
     */
    public UserConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "USER_CONFLICT");
    }

    /**
     * Constructs a new UserConflictException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UserConflictException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT, "USER_CONFLICT");
    }
}
