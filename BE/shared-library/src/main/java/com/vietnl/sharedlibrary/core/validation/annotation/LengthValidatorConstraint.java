package com.vietnl.sharedlibrary.core.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LengthValidatorConstraint implements ConstraintValidator<LengthValidator, String> {

  private int min;
  private int max;
  private String field;

  @Override
  public void initialize(LengthValidator constraintAnnotation) {
    min = constraintAnnotation.min();
    max = constraintAnnotation.max();
    field = constraintAnnotation.field();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // null thì không validate, để annotation khác xử lý @NotNull nếu cần
    }

    int length = value.trim().length();
    boolean valid = length >= min && length <= max;

    if (!valid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format("%s phải có độ dài từ %d đến %d ký tự", field, min, max))
          .addConstraintViolation();
    }

    return valid;
  }
}
