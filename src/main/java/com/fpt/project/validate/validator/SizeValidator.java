package com.fpt.project.validate.validator;

import com.fpt.project.validate.ValidateSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidator implements ConstraintValidator<ValidateSize, Integer> {
    int min;
    int max;

    @Override
    public void initialize(ValidateSize constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value >= min && value <= max;
    }
}
