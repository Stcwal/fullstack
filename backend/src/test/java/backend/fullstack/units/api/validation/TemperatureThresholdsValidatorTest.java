package backend.fullstack.units.api.validation;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import backend.fullstack.units.api.dto.UnitRequest;
import backend.fullstack.units.domain.UnitType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TemperatureThresholdsValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void validThresholdOrderPassesValidation() {
        UnitRequest request = new UnitRequest(
                "Freezer A",
                UnitType.FREEZER,
                3.0,
                1.0,
                5.0,
                "Main storage"
        );

        Set<ConstraintViolation<UnitRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidThresholdOrderProducesClassLevelViolation() {
        UnitRequest request = new UnitRequest(
                "Freezer A",
                UnitType.FREEZER,
                0.0,
                1.0,
                5.0,
                "Main storage"
        );

        Set<ConstraintViolation<UnitRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        ConstraintViolation<UnitRequest> violation = violations.iterator().next();
        assertEquals("Invalid thresholds: expected minThreshold < targetTemperature < maxThreshold",
                violation.getMessage());
    }
}