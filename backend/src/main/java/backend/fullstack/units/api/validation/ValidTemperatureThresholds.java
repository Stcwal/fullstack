package backend.fullstack.units.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = TemperatureThresholdsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTemperatureThresholds {

    String message() default "Invalid thresholds: expected minThreshold < targetTemperature < maxThreshold";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}