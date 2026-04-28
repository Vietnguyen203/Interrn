package com.vietnl.sharedlibrary.core.utils;

import java.util.UUID;

public class StringUtils {

  public static String capitalize(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.substring(0, 1).toUpperCase() + input.substring(1);
  }

  public static UUID safeParseUUID(String input) {
    if (input == null || input.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(input);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
