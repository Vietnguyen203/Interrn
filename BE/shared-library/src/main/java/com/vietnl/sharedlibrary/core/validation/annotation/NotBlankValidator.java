package com.vietnl.sharedlibrary.core.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBlankValidatorConstraint.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankValidator {

  String message() default "Invalid length";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String field() default "";
}
