package com.vietnl.sharedlibrary.core.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LengthValidatorConstraint.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LengthValidator {
  String message() default "Invalid length";

  int min() default 0;

  int max() default 255;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String field() default "";
}
