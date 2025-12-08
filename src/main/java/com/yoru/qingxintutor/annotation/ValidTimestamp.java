package com.yoru.qingxintutor.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;
import java.time.Instant;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimestampValidator.class)
@Documented
public @interface ValidTimestamp {
    String message() default "Timestamp is expired or too far in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxOffsetMillis() default 1000;    // 1秒
}

class TimestampValidator implements ConstraintValidator<ValidTimestamp, Instant> {
    private long maxOffsetMillis;

    @Override
    public void initialize(ValidTimestamp annotation) {
        this.maxOffsetMillis = annotation.maxOffsetMillis();
    }

    @Override
    public boolean isValid(@NotNull(message = "Invalid timestamp") Instant value,
                           ConstraintValidatorContext context) {
        if (value == null)
            return true;
        long now = System.currentTimeMillis();
        long timestamp = value.toEpochMilli();
        long diff = Math.abs(now - timestamp);
        return diff <= maxOffsetMillis;
    }
}