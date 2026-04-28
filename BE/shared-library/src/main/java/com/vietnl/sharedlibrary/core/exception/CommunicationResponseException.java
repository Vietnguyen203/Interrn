package com.vietnl.sharedlibrary.core.exception;

import com.eps.shared.core.json.JsonParserUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommunicationResponseException extends RuntimeException {

  public HttpStatus status;
  public Map<String, Object> body = new HashMap<>();

  public CommunicationResponseException(HttpStatus status, String body) {

    this.status = status;
    if (body != null) {
      this.body = JsonParserUtils.toObjectMap(body);
    }
  }
}
