package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is an error related to location operations, such as invalid location data or location not found.
 *
 * @version 1.0
 * @since 30.03.26
 */
public class LocationException extends AppException{

    /**
     * Constructs a new LocationException with the specified detail message.
     *
     * @param message the detail message
     */
    public LocationException(String message) {
        super(message, HttpStatus.CONFLICT,  "LOCATION_ERROR");
    }

    /**
     * Constructs a new LocationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public LocationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT,  "LOCATION_ERROR");
    }
}
