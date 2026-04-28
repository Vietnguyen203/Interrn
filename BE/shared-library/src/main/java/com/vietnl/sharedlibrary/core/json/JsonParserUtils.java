package com.vietnl.sharedlibrary.core.json;

import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import java.util.*;

public class JsonParserUtils {

  public static ObjectMapper getObjectMapper() {
    return JacksonConfig.getObjectMapper();
  }

  public static Map<String, String> toStringMap(String mapString) {
    try {
      return getObjectMapper().readValue(mapString, new TypeReference<>() {});
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  /**
   * @param mapString
   * @return
   */
  public static Map<String, Object> toObjectMap(String mapString) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> map =
          objectMapper.readValue(mapString, new TypeReference<Map<String, Object>>() {});
      return map;
    } catch (Exception e) {
      return new HashMap<>();
    }
  }

  public static <T> T entity(String json, Class<T> tClass) {
    try {

      return getObjectMapper().readValue(json, tClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T entity(String json, TypeReference<T> typeReference) {
    try {
      return getObjectMapper().readValue(json, typeReference);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes specified object as string
   *
   * @param object object to write
   * @return result json
   */
  public static String toJson(Object object) {
    try {
      return getObjectMapper().writeValueAsString(object);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> List<T> entityListFromJson(String json, Class<T> tClass) {
    try {
      ObjectMapper mapper = getObjectMapper();
      JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, tClass);
      return mapper.readValue(json, listType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, Object> flatten(String json, String prefix, String delimiter) {
    Map<String, Object> result = new HashMap<>();
    try {
      JsonNode root = getObjectMapper().readTree(json);

      flattenHelper(root, prefix, delimiter, result);
      return result;
    } catch (JsonProcessingException e) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          CommonErrorMessage.INVALID_JSON,
          new HashMap<>() {
            {
              put("json", json);
            }
          });
    }
  }

  private static void flattenHelper(
      JsonNode node, String prefix, String delimiter, Map<String, Object> result) {
    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        flattenHelper(
            field.getValue(),
            String.format("%s%s%s", prefix, delimiter, field.getKey()),
            delimiter,
            result);
      }
    } else if (node.isArray()) {
      List<Object> list = new ArrayList<>();
      for (int i = 0; i < node.size(); i++) {
        JsonNode item = node.get(i);
        list.add(convertValue(item));
      }
      result.put(prefix, list);
    } else {
      // Loại bỏ dấu chấm ở cuối prefix nếu có

      result.put(prefix, convertValue(node));
    }
  }

  private static Object convertValue(JsonNode node) {

    if (node.isTextual()) {
      return node.textValue();
    }

    if (node.isDouble()) {
      return node.doubleValue();
    }

    if (node.isFloat()) {
      return node.floatValue();
    }

    if (node.isLong()) {
      return node.longValue();
    }

    if (node.isInt()) {
      return node.intValue();
    }

    if (node.isBoolean()) {
      return node.booleanValue();
    }

    if (node.isNull()) {
      return null;
    }

    return node.textValue();
  }
}
