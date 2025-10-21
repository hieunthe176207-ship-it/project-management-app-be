package com.fpt.project.validate;


import com.fpt.project.validate.validator.PatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {PatternValidator.class})
public @interface PatternValidate {
    String message() default "Lỗi không đúng mẫu";


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
