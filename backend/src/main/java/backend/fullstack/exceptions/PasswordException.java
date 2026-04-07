package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error related to password operations, such as invalid password format or password mismatch.
 *
 * @version 1.0
 * @since 30.03.26
 */
public class PasswordException extends AppException {

    /**
     * Constructs a new PasswordException with the specified detail message.
     *
     * @param message the detail message
     */
    public PasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "PASSWORD_ERROR");
    }

    /**
     * Constructs a new PasswordException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PasswordException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "PASSWORD_ERROR");
    }
}
