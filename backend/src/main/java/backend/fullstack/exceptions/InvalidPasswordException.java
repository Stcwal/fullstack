package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when provided authentication credentials contain an invalid password.
 *
 * @version 1.0
 * @since 31.03.26
 */
public class InvalidPasswordException extends AppException {

    /**
     * Constructs a new InvalidPasswordException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidPasswordException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD");
    }
}
