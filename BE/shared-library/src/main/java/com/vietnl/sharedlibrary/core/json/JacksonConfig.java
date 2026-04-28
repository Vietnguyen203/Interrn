package com.vietnl.sharedlibrary.core.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Getter private static ObjectMapper objectMapper = null;

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper =
        JsonMapper.builder()

            // ===== CORE SETTINGS =====
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            // tránh crash khi API trả về null/empty linh tinh
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)

            // enum an toàn hơn khi backend thay đổi
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)

            // tránh lỗi khi số null -> primitive
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

            // ===== NAMING STRATEGY =====
            // nếu API dùng snake_case → auto map
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

            // ===== BUILD =====
            .build();

    // ===== JAVA TIME =====
    mapper.registerModule(new JavaTimeModule());

    // format ISO chuẩn (optional nếu bạn cần custom)
    mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    objectMapper = mapper;
    return mapper;
  }
}
