package com.vietnl.notificationservice.infrastructure.exception;

import com.vietnl.notificationservice.domain.models.enums.BaseErrorMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ResponseException extends RuntimeException {
    private final HttpStatus statusCode;
    private final BaseErrorMessage messageCode;
    private final Map<String, String> data;

    public ResponseException(HttpStatus statusCode, BaseErrorMessage messageCode) {
        super(messageCode.val());
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.data = null;
    }

    public ResponseException(HttpStatus statusCode, BaseErrorMessage messageCode, Map<String, String> data) {
        super(messageCode.val());
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.data = data;
    }

    public ResponseException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.messageCode = null;
        this.data = null;
    }
}
