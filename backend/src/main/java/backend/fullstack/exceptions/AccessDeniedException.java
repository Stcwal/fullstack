package backend.fullstack.exceptions;

/**
 * Exception thrown when a user attempts to access a resource or perform an action for which they do not have the necessary permissions.
 *
 * @version 1.0
 * @since 30.03.26
 */
public class AccessDeniedException extends AppException {

    /**
     * Constructs a new AccessDeniedException with the specified detail message.
     *
     * @param message the detail message
     */
    public AccessDeniedException(String message) {
        super(message, org.springframework.http.HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    /**
     * Constructs a new AccessDeniedException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause, org.springframework.http.HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
    
}