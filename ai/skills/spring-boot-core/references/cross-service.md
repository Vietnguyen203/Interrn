# Cross-Service Convention (theo backend/user-service)

## Vị trí
- Domain interfaces: `backend/user-service/src/main/java/com/eps/user/domain/services/communication/*`
- Infrastructure implementations: `backend/user-service/src/main/java/com/eps/user/infrastructure/communication/*`

## Pattern theo code thật

### 1) Domain Communication Interfaces
Các interface nằm trong `domain/services/communication`, ví dụ:
- `CategoryInternal`
- `FileInternal`
- `KeycloakUserInternal`
- `KeycloakSsoInternal`
- `NotificationCommunication`
- `OtpInternal`
- `WorkflowInternal`
- `VneIdInternal`

Đặc điểm:
- Interface đơn giản, chỉ khai báo method cần thiết
- Không có annotation Feign ở đây (để domain layer độc lập)

Ví dụ `CategoryInternal`:
```java
public interface CategoryInternal {
  SubData getWard(UUID id);
  SubData getProvince(UUID id);
  SubData getBusinessType(UUID id);
}
```

### 2) Infrastructure Implementation với FeignClient
Implementation nằm trong `infrastructure/communication`, dùng **Feign client**.

Ví dụ thực tế: `FeignNotificationCommunication`:
```java
@FeignClient(name = "notification-communication", url = "${app.properties.services.notification-service-url}")
public interface FeignNotificationCommunication extends NotificationCommunication {
  @Override
  @PostMapping("/internal/emails/send")
  void sendEmail(NotificationRequest request);
}
```

Pattern:
- `@FeignClient` với:
  - `name`: logical name của client
  - `url`: đọc từ `application.yml` (`${app.properties.services.notification-service-url}`)
- **extends** domain interface (`NotificationCommunication`)
- Override method với `@PostMapping` (hoặc `@GetMapping`, v.v.) và endpoint path

### 3) Service URLs trong application.yml
Định nghĩa trong `app.properties.services.*`:
```yaml
app.properties:
  services:
    workflow-service-url: ${WORKFLOW_SERVICE_URL}
    file-service-url: ${FILE_SERVICE_URL}
    otp-service-url: ${OTP_SERVICE_URL}
    app-url: ${WEB_URL}
    email-update: ${UPDATE_URL}
    notification-service-url: ${NOTIFICATION_SERVICE_URL}
    vneid-service-url: ${VNEID_URL}
    category-service-url: ${CATEGORY_SERVICE_URL:http://category-service:8080}
```

### 4) HeaderContext & Authorization Forwarding
Khi gọi service khác, cần truyền Authorization header:
- Lấy từ `HeaderContext.extraData(HttpHeaders.AUTHORIZATION)`
- Thêm vào request của Feign client

Pattern trong controller/service:
```java
context.getExtraData().put(HttpHeaders.AUTHORIZATION, (String) headers.get(HeaderKeys.AUTHORIZATION));
```

### 5) External Service Wrapping
Dùng `KeycloakCatcher.run()` để wrap external service calls:
```java
String ssoId = KeycloakCatcher.run(() -> keycloakUserInternal.create(value));
```

### 6) Notification Communication
Gửi notification qua `NotificationCommunication`:
- Build `NotificationRequest` với state, title, type, dataJson, recipientIds, senderId, content
- Gọi `communication.sendEmail()` hoặc tương tự

Ví dụ trong `EmailService`:
```java
communication.sendEmail(
    NotificationRequest.builder()
        .state(state)
        .title(title)
        .type(type)
        .dataJson(dataJson)
        .recipientIds(List.copyOf(recipientIds))
        .senderId(senderId)
        .content(new String(Files.readAllBytes(resourceLoader.getResource("classpath:templates/" + templateName).getFile().toPath())))
        .build());
```

### 7) Async Email Sending
Dùng `@Async` để gửi email không blocking:
```java
@Async
public void sendEmail(String state, String type, String senderId, Collection<String> recipientIds, Map<String, String> dataJson, String templateName, String title) {
  // implementation
}
```

### 8) Feign Client Configuration
Đã cấu hình trong `application.yml`:
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 50000
            readTimeout: 2000000
```

## Tham chiếu file thật
- `backend/user-service/src/main/java/com/eps/user/domain/services/communication/CategoryInternal.java`
- `backend/user-service/src/main/java/com/eps/user/infrastructure/communication/FeignNotificationCommunication.java`
- `backend/user-service/src/main/java/com/eps/user/application/usecases/EmailService.java`
- `backend/user-service/src/main/resources/application.yml` (app.properties.services.*)