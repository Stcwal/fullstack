package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a temperature unit cannot be found by identifier.
 *
 * @version 1.0
 * @since 31.03.26
 */
public class UnitNotFoundException extends AppException {

    /**
     * Constructs a new UnitNotFoundException for the missing temperature unit identifier.
     *
     * @param unitId the missing temperature unit identifier
     */
    public UnitNotFoundException(Long unitId) {
        super("Temperature unit not found with id: " + unitId, HttpStatus.NOT_FOUND, "UNIT_NOT_FOUND");
    }
}