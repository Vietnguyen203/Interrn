package com.vietnl.sharedlibrary.core.utils;

import com.eps.shared.core.valueobject.PositionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Slf4j
public class GenericTypeUtils {

  public static <T> Object getFieldValue(T target, String fieldName) {

    Field field = ReflectionUtils.findField(target.getClass(), fieldName);
    if (field == null) {
      throw new RuntimeException(
          "Could not find field " + fieldName + " in class " + target.getClass());
    }
    field.setAccessible(true);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> void updateData(T target, String fieldName, Object value) {
    try {
      Field field = ReflectionUtils.findField(target.getClass(), fieldName);
      if (field != null) {
        field.setAccessible(true);
        field.set(target, value);
      }
    } catch (Exception ignore) {
    }
  }

  @SuppressWarnings("unchecked")
  public static <T, S> T getNewInstance(S superClass, Class<?> clazz, PositionType type) {
    try {
      Class<?> currentClass = superClass.getClass();
      Type targetGenericType = findGenericTypeInHierarchy(currentClass, clazz);

      if (targetGenericType instanceof ParameterizedType parameterizedType) {
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        int index =
            type.equals(PositionType.LAST) ? actualTypeArguments.length - 1 : type.getValue();
        if (index < actualTypeArguments.length) {
          Type targetType = actualTypeArguments[index];

          if (targetType instanceof Class) {
            Class<T> targetClass = (Class<T>) targetType;

            // Tạo instance mới bằng constructor mặc định
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
          }
        }
      }

      throw new IllegalArgumentException(
          "Không thể xác định kiểu generic tại vị trí "
              + type
              + " cho interface "
              + clazz.getSimpleName());

    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi tạo instance: " + e.getMessage(), e);
    }
  }

  // Phương thức helper để tìm kiếm generic type trong toàn bộ hierarchy
  private static Type findGenericTypeInHierarchy(Class<?> startClass, Class<?> targetInterface) {
    // Tìm trong superclass hierarchy
    Class<?> currentClass = startClass;
    while (currentClass != null) {
      // Kiểm tra generic superclass
      Type genericSuperclass = currentClass.getGenericSuperclass();
      if (genericSuperclass instanceof ParameterizedType) {
        ParameterizedType paramType = (ParameterizedType) genericSuperclass;
        if (isAssignableFrom(targetInterface, (Class<?>) paramType.getRawType())) {
          return genericSuperclass;
        }
      }

      // Kiểm tra interfaces của class hiện tại
      Type foundType = searchInInterfaces(currentClass.getGenericInterfaces(), targetInterface);
      if (foundType != null) {
        return foundType;
      }

      currentClass = currentClass.getSuperclass();
    }

    return null;
  }

  // Tìm kiếm trong danh sách interfaces (bao gồm cả interface con)
  private static Type searchInInterfaces(Type[] interfaces, Class<?> targetInterface) {
    for (Type interfaceType : interfaces) {
      if (interfaceType instanceof ParameterizedType) {
        ParameterizedType paramType = (ParameterizedType) interfaceType;
        Class<?> rawType = (Class<?>) paramType.getRawType();

        // Kiểm tra trực tiếp
        if (isAssignableFrom(targetInterface, rawType)) {
          return interfaceType;
        }

        // Tìm kiếm recursive trong các interface cha
        Type foundInParent = searchInInterfaces(rawType.getGenericInterfaces(), targetInterface);
        if (foundInParent != null) {
          return foundInParent;
        }
      } else if (interfaceType instanceof Class) {
        Class<?> rawType = (Class<?>) interfaceType;

        // Kiểm tra trực tiếp
        if (isAssignableFrom(targetInterface, rawType)) {
          return interfaceType;
        }

        // Tìm kiếm recursive trong các interface cha
        Type foundInParent = searchInInterfaces(rawType.getGenericInterfaces(), targetInterface);
        if (foundInParent != null) {
          return foundInParent;
        }
      }
    }
    return null;
  }

  // Helper method để kiểm tra inheritance/implementation
  private static boolean isAssignableFrom(Class<?> target, Class<?> candidate) {
    return target.isAssignableFrom(candidate) || candidate.isAssignableFrom(target);
  }
}
