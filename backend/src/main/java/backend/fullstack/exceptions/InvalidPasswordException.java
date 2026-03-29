package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

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
