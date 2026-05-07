package com.vietnl.notificationservice.infrastructure.exception;

import com.vietnl.notificationservice.domain.models.enums.BaseErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
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
    private String message;
    private String path;
    private String method;
    private List<FieldErrorDetail> errors;

    @Data
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
