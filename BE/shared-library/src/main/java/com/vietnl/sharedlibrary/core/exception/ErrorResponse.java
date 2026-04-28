package com.vietnl.sharedlibrary.core.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String traceId;
  private LocalDateTime timestamp;
  private int status;
  private HttpStatus statusCode;
  private BaseErrorMessage messageCode;
  //  private String error;
  private String message;
  private String path;
  private List<FieldErrorDetail> errors;
  private String method;

  // Constructors, Getters, Setters

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FieldErrorDetail {
    private String field;
    private String message;
    private Object rejectedValue;

    // Constructors, Getters, Setters
  }

  public ErrorResponse(
      String path, String method, HttpStatus statusCode, BaseErrorMessage messageCode) {
    timestamp = LocalDateTime.now();

    this.statusCode = statusCode;
    this.messageCode = messageCode;
    message = messageCode.val();
    this.path = path;
    errors = new ArrayList<>();
    status = statusCode.value();
  }
}
