package com.vietnl.sharedlibrary.core.utils;

import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.core.valueobject.IEnum;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.HashMap;

public class EnumUtils {

  @SuppressWarnings("unchecked")
  public static Object convertEnum(Class<?> clazz, Object value) {
    if (value == null || !StringUtils.hasText(value.toString())) {
      return null;
    }
    try {
      return Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), value.toString());
    } catch (Exception ignore) {

    }

    throw new ResponseException(
        HttpStatus.BAD_REQUEST,
        CommonErrorMessage.ENUM_FAILED,
        new HashMap<>() {
          {
            put("value", value.toString());
            put("type", clazz.getSimpleName());
          }
        });
  }

  public static <E extends Enum<E> & IEnum> E fromValue(Class<E> clazz, Byte value) {
    for (E e : clazz.getEnumConstants()) {
      if (e.getValue() == value) {
        return e;
      }
    }

    throw new ResponseException(
        HttpStatus.BAD_REQUEST,
        CommonErrorMessage.ENUM_FAILED,
        new HashMap<>() {
          {
            put("value", value.toString());
            put("type", clazz.getSimpleName());
          }
        });
  }
}
