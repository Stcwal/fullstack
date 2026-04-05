package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidThresholdException extends AppException {

    public InvalidThresholdException() {
        super("Invalid thresholds: expected minThreshold < targetTemperature < maxThreshold", HttpStatus.BAD_REQUEST,
                "INVALID_THRESHOLD");
    }
}