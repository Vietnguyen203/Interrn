package com.vietnl.sharedlibrary.core.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotNullValidatorConstraint implements ConstraintValidator<NotNullValidator, String> {

  private String field;

  @Override
  public void initialize(NotNullValidator constraintAnnotation) {
    field = constraintAnnotation.field();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(String.format("%s là bắt buộc", field))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
