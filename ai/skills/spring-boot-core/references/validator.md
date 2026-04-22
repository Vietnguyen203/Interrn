# Validator Convention

## Mục tiêu
Tách business validation ra khỏi service, đặt trong `application/validators`.

## Vị trí
`backend/user-service/src/main/java/com/eps/user/application/validators/*`

## Pattern theo code thật
Ví dụ thực tế: `NguoiDungValidator`.

### 1) Khai báo class
- `@Service`
- Lombok: `@AllArgsConstructor`
- Inject `...Persistence`

```java
@Service
@AllArgsConstructor
public class NguoiDungValidator {
  private final UserAccountPersistence persistence;
  // ...
}
```

### 2) Validate create
- Check trùng theo field (ví dụ `userCode`)
- Điều kiện check thường dùng `StringUtils.hasText(...)`
- Khi duplicate: throw `ResponseException(HttpStatus.BAD_REQUEST, ExceptionMessage.USER_DUPLICATE)`

Theo code thật:
```java
public void validateCreate(UserAccountRequest request) {
  if (StringUtils.hasText(request.getUserCode())) {
    checkExistence(
        () -> persistence.existsByUserCode(request.getUserCode()),
        ExceptionMessage.USER_DUPLICATE);
  }
}
```

### 3) Validate update
- Load entity hiện tại trước
- Nếu không có: throw NOT_FOUND

Theo code thật:
```java
public void validateUpdate(UUID id, UserAccountRequest request) {
  UserAccount current =
      persistence
          .findById(id)
          .orElseThrow(
              () -> new ResponseException(HttpStatus.NOT_FOUND, ExceptionMessage.USER_NOT_FOUND));

  // Các check khác thường chỉ bật khi cần (nhiều đoạn đang comment)
}
```

### 4) Helper checkExistence
Theo code thật:
```java
private void checkExistence(Supplier<Boolean> condition, BaseErrorMessage message) {
  if (condition.get()) {
    throw new ResponseException(HttpStatus.BAD_REQUEST, message);
  }
}
```

### 5) Validate entity exists
Theo code thật (`validateUserNull`):
```java
public void validateUserNull(UUID id) {
  persistence
      .findById(id)
      .orElseThrow(
          () -> new ResponseException(HttpStatus.NOT_FOUND, ExceptionMessage.USER_NOT_FOUND));
}
```

## Exception/Message
- Exception class: `com.eps.shared.models.exceptions.ResponseException`
- Message enum: `com.eps.user.domain.models.enums.ExceptionMessage implements BaseErrorMessage`

## Ghi chú
- Validator **không** nên gọi external service; chỉ validate dữ liệu và trạng thái entity.
- Những validation phức tạp (unique nhiều field) thường check theo kiểu "nếu field thay đổi thì mới check" (trong code thật có các đoạn comment minh hoạ).

## Template
Xem `assets/templates/service-template.java` để tạo validator mới theo chuẩn (hoặc tạo file validator thủ công theo pattern trong tài liệu này).
