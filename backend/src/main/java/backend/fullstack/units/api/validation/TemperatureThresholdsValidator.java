package backend.fullstack.units.api.validation;

import backend.fullstack.units.api.dto.UnitRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TemperatureThresholdsValidator implements ConstraintValidator<ValidTemperatureThresholds, UnitRequest> {

    @Override
    public boolean isValid(UnitRequest request, ConstraintValidatorContext context) {
        if (request == null
                || request.minThreshold() == null
                || request.targetTemperature() == null
                || request.maxThreshold() == null) {
            return true;
        }

        boolean valid = request.minThreshold() < request.targetTemperature()
                && request.targetTemperature() < request.maxThreshold();

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
        }

        return valid;
    }
}