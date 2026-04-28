package com.vietnl.sharedlibrary.core.exception;

import com.eps.shared.core.utils.KeywordReplacer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ResponseException extends RuntimeException {
  HttpStatus statusCode;
  BaseErrorMessage messageCode;

  public ResponseException(HttpStatus statusCode, BaseErrorMessage msg) {
    super(msg.val());
    this.statusCode = statusCode;
    messageCode = msg;
  }

  public ResponseException(HttpStatus statusCode, BaseErrorMessage msg, Map<String, String> data) {
    super(KeywordReplacer.replaceKeywords(msg.val(), data));
    this.statusCode = statusCode;
    messageCode = msg;
  }

  public ResponseException(HttpStatus statusCode, String msg) {
    super(msg);
    this.statusCode = statusCode;
  }
}
