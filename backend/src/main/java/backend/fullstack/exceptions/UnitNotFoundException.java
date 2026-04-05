package backend.fullstack.exceptions;

import org.springframework.http.HttpStatus;

public class UnitNotFoundException extends AppException {

    public UnitNotFoundException(Long unitId) {
        super("Temperature unit not found with id: " + unitId, HttpStatus.NOT_FOUND, "UNIT_NOT_FOUND");
    }
}