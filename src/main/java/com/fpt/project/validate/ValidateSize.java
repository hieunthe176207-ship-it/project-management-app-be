package com.fpt.project.validate;

import com.fpt.project.validate.validator.SizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SizeValidator.class})
public @interface ValidateSize {
    String message() default "Size không hợp lệ";

    int min();

    int max();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
