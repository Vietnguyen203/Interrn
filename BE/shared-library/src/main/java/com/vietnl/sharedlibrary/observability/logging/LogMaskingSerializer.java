package com.vietnl.sharedlibrary.observability.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Set;

public class LogMaskingSerializer extends JsonSerializer<String> {

  private static final Set<String> MASKED_FIELDS =
      Set.of("password", "email", "phone", "card", "ccv", "token", "secret", "authorization");

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    String fieldName = gen.getOutputContext().getCurrentName();

    if (fieldName != null && isSensitive(fieldName.toLowerCase())) {
      gen.writeString("****MASKED****");
    } else {
      gen.writeString(value);
    }
  }

  private boolean isSensitive(String fieldName) {
    return MASKED_FIELDS.stream().anyMatch(fieldName::contains);
  }
}
