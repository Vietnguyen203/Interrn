# Exception Handling Convention

## Mục tiêu
Sử dụng exception thống nhất từ shared library: `ResponseException` + `BaseErrorMessage`.  
Global exception handler sẽ bắt và trả về `ErrorResponse` format chuẩn.

## Vị trí
- Shared exception classes: `backend/shared-library/src/main/java/com/eps/shared/models/exceptions/`
- Global handler: `interfaces/rest/GlobalExceptionHandler.java` (thường đặt ở mỗi service)
- Message enums: `{service}/domain/models/enums/ExceptionMessage implements BaseErrorMessage`

## Core classes from shared library

### 1) ResponseException
```java
public class ResponseException extends RuntimeException {
  HttpStatus statusCode;
  BaseErrorMessage messageCode;

  // Constructors
  public ResponseException(HttpStatus statusCode, BaseErrorMessage msg) { ... }
  public ResponseException(HttpStatus statusCode, BaseErrorMessage msg, Map<String, String> data) { ... }
  public ResponseException(HttpStatus statusCode, String msg) { ... }
}
```

### 2) BaseErrorMessage (interface)
```java
public interface BaseErrorMessage {
  String val();
}
```

### 3) CommonErrorMessage (enum dùng chung)
```java
public enum CommonErrorMessage implements BaseErrorMessage {
  FORBIDDEN("Bạn không có quyền truy cập"),
  VALIDATION_FAILED("Xác minh dữ liệu thất bại"),
  NOT_FOUND("Không tìm thấy dữ liệu"),
  INTERNAL_SERVER("Hệ thống có lỗi xảy ra xin vui lòng thử lại sau"),
  // ...
}
```

### 4) ErrorResponse (format trả về API)
```java
@Data @Builder
public class ErrorResponse {
  private String traceId;
  private LocalDateTime timestamp;
  private int status;
  private HttpStatus statusCode;
  private BaseErrorMessage messageCode;
  private String message;
  private String path;
  private List<FieldErrorDetail> errors;
  private String method;
}
```

## Pattern theo code thật

### 1) Định nghĩa ExceptionMessage trong service
```java
public enum ExceptionMessage implements BaseErrorMessage {
  USER_NOT_FOUND("Không tìm thấy người dùng"),
  USER_DUPLICATE("Mã người dùng đã tồn tại"),
  INVALID_STATUS("Trạng thái không hợp lệ");

  private final String val;

  ExceptionMessage(String val) {
    this.val = val;
  }

  @Override
  public String val() {
    return val;
  }
}
```

### 2) Throw exception trong service/validator
```java
// Ví dụ trong validator hoặc service
throw new ResponseException(HttpStatus.NOT_FOUND, ExceptionMessage.USER_NOT_FOUND);

// Hoặc có placeholder
Map<String, String> data = Map.of("fieldname", "userCode");
throw new ResponseException(HttpStatus.BAD_REQUEST, CommonErrorMessage.FIELD_CANT_SORT, data);
```

### 3) GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseException.class)
  public ResponseEntity<ErrorResponse> handleResponseException(ResponseException ex, WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .statusCode(ex.getStatusCode())
        .messageCode(ex.getMessageCode())
        .message(ex.getMessage())
        .path(((ServletWebRequest) request).getRequest().getRequestURI())
        .status(ex.getStatusCode().value())
        .build();
    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
    List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> new FieldErrorDetail(err.getField(), err.getDefaultMessage(), err.getRejectedValue()))
        .collect(Collectors.toList());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .statusCode(HttpStatus.BAD_REQUEST)
        .messageCode(CommonErrorMessage.VALIDATION_FAILED)
        .message(CommonErrorMessage.VALIDATION_FAILED.val())
        .path(((ServletWebRequest) request).getRequest().getRequestURI())
        .errors(errors)
        .status(HttpStatus.BAD_REQUEST.value())
        .build();
    return ResponseEntity.badRequest().body(error);
  }
}
```

## Quy tắc sử dụng

### Khi nào dùng ResponseException
- Business validation failed (duplicate, not found, invalid state)
- Authorization/permission errors
- Custom domain errors

### Khi nào dùng CommonErrorMessage
- Errors dùng chung nhiều service: NOT_FOUND, VALIDATION_FAILED, INTERNAL_SERVER, v.v.
- Field placeholder errors: FIELD_CANT_SORT, ENUM_FAILED, INVALID_JSON

### Placeholder trong message
Dùng `KeywordReplacer.replaceKeywords(message, data)` để thay thế placeholder:
```java
Map<String, String> data = Map.of("fieldname", "userCode", "value", "abc");
throw new ResponseException(HttpStatus.BAD_REQUEST, CommonErrorMessage.ENUM_FAILED, data);
// Result: "Không thể chuyển đổi abc thành loại #type#"
```

## Logging exception
- Global handler nên log exception với traceId
- Dùng structured logging (logback) với level ERROR cho business exceptions, WARN cho validation

## Template
- Exception message enum: `assets/templates/enum-template.java` (implements BaseErrorMessage)
- Global exception handler: cần tạo template riêng nếu muốn generate nhanh

## Ghi chú
- Không throw raw Exception; luôn dùng `ResponseException` với `BaseErrorMessage`
- Enum message nên đặt tên rõ ràng: `{ENTITY}_{ACTION}_{RESULT}` (ví dụ: USER_NOT_FOUND)
- Global handler đảm bảo format API response nhất quán across services