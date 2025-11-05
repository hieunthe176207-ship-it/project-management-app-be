package com.fpt.project.validate.validator;

import com.fpt.project.validate.PatternValidate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PatternValidator implements ConstraintValidator<PatternValidate, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.matches("HE\\d{6}");
    }
}
