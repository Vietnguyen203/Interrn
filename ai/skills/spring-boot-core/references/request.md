# Request DTO Convention

## Mục tiêu
Tài liệu này mô tả **chuẩn Request DTO** với validation rules dựa trên shared-library annotations.

---

## 📦 Package Locations

| Component | Package |
|-----------|---------|
| Request DTOs | `{package}.application.requests` |
| Custom Validators | `com.eps.shared.annotations` |
| Validation Groups | `com.eps.shared.models.values.requests` |

---

## 🏛️ Class Structure

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ${Entity}Request {
  // fields with validation...
}
```

---

## 📏 Field Length Rules (Database Mapping)

| Field Type | DB Type | Max Length | Annotation |
|------------|---------|------------|------------|
| `name` | `NVARCHAR(255)` | 255 | `@LengthValidator(max = 255, field = "Tên")` |
| `code` | `VARCHAR(100)` | 100 | `@LengthValidator(max = 100, field = "Mã")` |
| `description` | `NVARCHAR(1024)` | 1024 | `@LengthValidator(max = 1024, field = "Mô tả")` |
| `note` | `NVARCHAR(1024)` | 1024 | `@LengthValidator(max = 1024, field = "Ghi chú")` |
| `jsonData` | `CLOB (@Lob)` | unlimited | Không cần `@LengthValidator` |
| **Các trường khác** | `NVARCHAR(255)` | 255 | `@LengthValidator(max = 255, field = "...")` |

---

## ✅ Shared-Library Custom Annotations

### 1. `@LengthValidator` - Kiểm tra độ dài
**Package:** `com.eps.shared.annotations.LengthValidator`

```java
@LengthValidator(min = 0, max = 255, field = "Tên người dùng")
private String fullName;
```

**Attributes:**
| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `min` | int | 0 | Độ dài tối thiểu |
| `max` | int | 255 | Độ dài tối đa |
| `field` | String | "" | Tên field hiển thị trong message lỗi |
| `groups` | Class<?>[] | {} | Validation groups |
| `message` | String | "Invalid length" | Custom message (override auto-message) |

**Auto-generated message:** `"{field} phải có độ dài từ {min} đến {max} ký tự"`

**Lưu ý:** Annotation này **cho phép null** - nếu value là null sẽ pass validation. Kết hợp với `@NotBlankValidator` hoặc `@NotNullValidator` nếu field bắt buộc.

---

### 2. `@NotBlankValidator` - Kiểm tra không rỗng
**Package:** `com.eps.shared.annotations.NotBlankValidator`

```java
@NotBlankValidator(field = "Tên người dùng")
private String fullName;
```

**Attributes:**
| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `field` | String | "" | Tên field hiển thị trong message lỗi |
| `groups` | Class<?>[] | {} | Validation groups |
| `message` | String | "Invalid length" | Custom message |

**Auto-generated message:** `"{field} là bắt buộc"`

**Behavior:** Trả về `false` nếu value là `null` hoặc `value.trim().isEmpty()`

---

### 3. `@NotNullValidator` - Kiểm tra không null
**Package:** `com.eps.shared.annotations.NotNullValidator`

```java
@NotNullValidator(field = "Đơn vị")
private UUID orgId;
```

**Attributes:**
| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `field` | String | "" | Tên field hiển thị trong message lỗi |
| `groups` | Class<?>[] | {} | Validation groups |
| `message` | String | "Invalid length" | Custom message |

**Auto-generated message:** `"{field} là bắt buộc"`

---

## 📝 Complete Request Template

```java
package ${PACKAGE}.application.requests;

import com.eps.shared.annotations.LengthValidator;
import com.eps.shared.annotations.NotBlankValidator;
import com.eps.shared.annotations.NotNullValidator;
import com.eps.shared.models.values.requests.OnCreate;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.AssertTrue;
import java.util.UUID;
import lombok.*;
import org.springframework.util.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ${Entity}Request {

  // ==================== CODE: VARCHAR(100) ====================
  @NotBlankValidator(field = "Mã", groups = {OnCreate.class})
  @LengthValidator(max = 100, field = "Mã")
  private String code;

  // ==================== NAME: NVARCHAR(255) ====================
  @NotBlankValidator(field = "Tên")
  @LengthValidator(max = 255, field = "Tên")
  private String name;

  // ==================== DESCRIPTION: NVARCHAR(1024) ====================
  @LengthValidator(max = 1024, field = "Mô tả")
  private String description;

  // ==================== NOTE: NVARCHAR(1024) ====================
  @LengthValidator(max = 1024, field = "Ghi chú")
  private String note;

  // ==================== JSON DATA: CLOB ====================
  @Lob
  private String jsonData;  // Không cần LengthValidator

  // ==================== OTHER STRING FIELDS: NVARCHAR(255) ====================
  @LengthValidator(max = 255, field = "Địa chỉ")
  private String address;

  @LengthValidator(max = 255, field = "Email")
  private String email;

  @LengthValidator(max = 255, field = "Số điện thoại")
  private String phone;

  // ==================== REFERENCE FIELDS ====================
  @NotNullValidator(field = "Đơn vị", groups = {OnCreate.class})
  private UUID orgId;

  private UUID parentId;

  // ==================== PASSWORD (OnCreate only) ====================
  @NotBlankValidator(field = "Mật khẩu", groups = {OnCreate.class})
  @LengthValidator(min = 8, max = 255, field = "Mật khẩu", groups = {OnCreate.class})
  private String passwd;

  private String confirmPasswd;

  // ==================== CUSTOM VALIDATIONS ====================

  @AssertTrue(message = "Mật khẩu xác thực không hợp lệ", groups = OnCreate.class)
  public boolean isValidConfirmPassword() {
    if (!StringUtils.hasLength(passwd)) {
      return true;
    }
    return passwd.equals(confirmPasswd);
  }

  @AssertTrue(message = "Đơn vị là bắt buộc khi loại là ORG")
  public boolean isValidOrgType() {
    // Ví dụ: bắt buộc orgId khi type = ORG
    return type != ${Entity}Type.ORG || orgId != null;
  }
}
```

---

## 🔗 Validation Groups

### `OnCreate`
**Package:** `com.eps.shared.models.values.requests.OnCreate`

Dùng cho validation chỉ áp dụng khi **tạo mới**:

```java
// Chỉ validate khi create
@NotBlankValidator(field = "Mật khẩu", groups = {OnCreate.class})
private String passwd;

// Validate cả create và update
@NotBlankValidator(field = "Tên")
private String name;
```

**Controller usage:**
```java
@PostMapping
public ResponseEntity<?> create(
    @RequestBody @Validated({OnCreate.class, Default.class}) ${Entity}Request request
) { ... }

@PutMapping("/{id}")
public ResponseEntity<?> update(
    @PathVariable UUID id,
    @RequestBody @Validated ${Entity}Request request  // Không có OnCreate
) { ... }
```

---

## 📋 Quick Reference

### Validation Annotations Comparison

| Annotation | Package | Null Allowed | Empty Allowed | Auto-message |
|------------|---------|--------------|---------------|--------------|
| `@LengthValidator` | `com.eps.shared.annotations` | ✅ Yes | ✅ Yes | `{field} phải có độ dài từ {min} đến {max} ký tự` |
| `@NotBlankValidator` | `com.eps.shared.annotations` | ❌ No | ❌ No | `{field} là bắt buộc` |
| `@NotNullValidator` | `com.eps.shared.annotations` | ❌ No | ✅ Yes | `{field} là bắt buộc` |
| `@NotNull` | `jakarta.validation` | ❌ No | ✅ Yes | Custom message |
| `@NotBlank` | `jakarta.validation` | ❌ No | ❌ No | Custom message |
| `@Length` | `org.hibernate.validator` | ✅ Yes | ✅ Yes | Custom message |

### Common Field Patterns

```java
// Tên (bắt buộc, max 255)
@NotBlankValidator(field = "Tên")
@LengthValidator(max = 255, field = "Tên")
private String name;

// Mã (bắt buộc khi tạo, max 100)
@NotBlankValidator(field = "Mã", groups = {OnCreate.class})
@LengthValidator(max = 100, field = "Mã")
private String code;

// Mô tả (optional, max 1024)
@LengthValidator(max = 1024, field = "Mô tả")
private String description;

// JSON data (CLOB - no length limit)
@Lob
private String jsonData;

// UUID reference (bắt buộc)
@NotNullValidator(field = "Đơn vị")
private UUID orgId;

// UUID reference (optional)
private UUID parentId;
```

---

## 📁 File Locations

| Component | Location |
|-----------|----------|
| LengthValidator | `com.eps.shared.annotations.LengthValidator` |
| LengthValidatorConstraint | `com.eps.shared.annotations.LengthValidatorConstraint` |
| NotBlankValidator | `com.eps.shared.annotations.NotBlankValidator` |
| NotBlankValidatorConstraint | `com.eps.shared.annotations.NotBlankValidatorConstraint` |
| NotNullValidator | `com.eps.shared.annotations.NotNullValidator` |
| NotNullValidatorConstraint | `com.eps.shared.annotations.NotNullValidatorConstraint` |
| OnCreate | `com.eps.shared.models.values.requests.OnCreate` |
