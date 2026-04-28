package com.vietnl.sharedlibrary.core.mapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;

public class FnCommon {

  public static void copyProperties(Object target, Object source) {
    //        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    BeanUtils.copyProperties(source, target);
  }

  public static void copyAllProperties(Object target, Object source) {
    BeanUtils.copyProperties(source, target);
  }

  public static <T> T copyAllProperties(Class<T> clazz, Object source) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      T target = constructor.newInstance();
      BeanUtils.copyProperties(source, target);
      return target;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void copyNotNullProperties(Object target, Object source) {
    BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
  }

  public static <T> T copyNonNullProperties(Class<T> clazz, Object source) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      T target = constructor.newInstance();
      BeanUtils.copyProperties(source, target, getNullPropertyNames(source));

      return target;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T copyProperties(Class<T> clazz, Object source) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      T target = constructor.newInstance();
      BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
      return target;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String[] getNullPropertyNames(Object source) {
    BeanWrapper wrappedSource = new BeanWrapperImpl(source);
    return Stream.of(wrappedSource.getPropertyDescriptors())
        .map(FeatureDescriptor::getName)
        .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
        .toArray(String[]::new);
  }

  public static boolean notNullOrBlank(String str) {
    return str != null && !str.trim().isEmpty();
  }
}
