package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

public class UnitInactiveException extends AppException {

    public UnitInactiveException(Long unitId) {
        super("Temperature unit is inactive: " + unitId, HttpStatus.CONFLICT, "UNIT_INACTIVE");
    }
}