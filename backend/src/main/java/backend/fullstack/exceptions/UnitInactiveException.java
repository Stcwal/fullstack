package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation targets a temperature unit that exists but is inactive.
 *
 * @version 1.0
 * @since 31.03.26
 */
public class UnitInactiveException extends AppException {

    /**
     * Constructs a new UnitInactiveException for the provided temperature unit identifier.
     *
     * @param unitId the inactive temperature unit identifier
     */
    public UnitInactiveException(Long unitId) {
        super("Temperature unit is inactive: " + unitId, HttpStatus.CONFLICT, "UNIT_INACTIVE");
    }
}