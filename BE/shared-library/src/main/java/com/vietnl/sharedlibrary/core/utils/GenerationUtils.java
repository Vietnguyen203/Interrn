package com.vietnl.sharedlibrary.core.utils;

import com.github.f4b6a3.uuid.UuidCreator;
import java.lang.reflect.Constructor;
import java.util.UUID;

public class GenerationUtils {

  public static UUID randomUUID() {

    return UuidCreator.getTimeOrderedEpoch();
  }

  public static <T> T newInstance(Class<T> clazz) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
