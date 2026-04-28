package com.vietnl.sharedlibrary.data.jpa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
  private static final String SPLIT_CHAR = ";";

  @Override
  public String convertToDatabaseColumn(List<String> list) {
    return list == null || list.isEmpty()
        ? ""
        : list.stream().map(String::trim).collect(Collectors.joining(SPLIT_CHAR));
  }

  @Override
  public List<String> convertToEntityAttribute(String joined) {
    return (joined == null || joined.isBlank())
        ? Collections.emptyList()
        : Arrays.stream(joined.split(SPLIT_CHAR)).map(String::trim).collect(Collectors.toList());
  }
}
