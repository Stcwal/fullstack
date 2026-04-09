package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when provided threshold values do not satisfy required ordering constraints.
 *
 * @version 1.0
 * @since 31.03.26
 */
public class InvalidThresholdException extends AppException {

    /**
     * Constructs a new InvalidThresholdException with a standard validation message.
     */
    public InvalidThresholdException() {
        super("Invalid thresholds: expected minThreshold < targetTemperature < maxThreshold", HttpStatus.BAD_REQUEST,
                "INVALID_THRESHOLD");
    }
}