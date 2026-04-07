package backend.fullstack.temperature.api.dto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TemperatureReadingRequestValidationTest {

    private final Validator validator;

    TemperatureReadingRequestValidationTest() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    @Test
    void validRequestPassesValidation() {
        TemperatureReadingRequest request = new TemperatureReadingRequest(2.5, null, "OK");

        Set<ConstraintViolation<TemperatureReadingRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void missingTemperatureFailsValidation() {
        TemperatureReadingRequest request = new TemperatureReadingRequest(null, null, "OK");

        Set<ConstraintViolation<TemperatureReadingRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        ConstraintViolation<TemperatureReadingRequest> violation = violations.iterator().next();
        assertEquals("Temperature is required", violation.getMessage());
    }
}