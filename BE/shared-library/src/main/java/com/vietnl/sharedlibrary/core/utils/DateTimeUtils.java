package com.vietnl.sharedlibrary.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class DateTimeUtils {
  private static final List<String> SUPPORTED_PATTERNS =
      Arrays.asList(
          "yyyy-MM-dd HH:mm:ss",
          "yyyy-MM-dd'T'HH:mm:ss",
          "yyyy/MM/dd HH:mm:ss",
          "dd/MM/yyyy HH:mm:ss",
          "yyyy-MM-dd HH:mm:ss.SSS",
          "yyyy-MM-dd'T'HH:mm:ss.SSS",
          "yyyyMMddHHmmss");

  /** Convert String to LocalDateTime. */
  public static LocalDateTime toLocalDateTime(String input) {
    if (input == null || input.isBlank()) {
      return null;
    }

    for (String pattern : SUPPORTED_PATTERNS) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(input, formatter);
      } catch (DateTimeParseException ignore) {
      }
    }

    throw new IllegalArgumentException("Unsupported datetime format: " + input);
  }
}
