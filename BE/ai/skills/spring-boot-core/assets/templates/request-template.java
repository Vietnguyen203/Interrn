package ${PACKAGE}.application.requests;

import com.eps.shared.models.values.requests.OnCreate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ${ENTITY}Request {

  @NotNull(message = "Tên là bắt buộc")
  @Size(max = 255, message = "Tên không quá 255 ký tự")
  private String name;

  @Size(max = 100, message = "Mã không quá 100 ký tự")
  private String code;

  @Size(max = 500, message = "Mô tả không quá 500 ký tự")
  private String description;

  private Boolean isActive = true;

  @NotNull(message = "Mật khẩu là bắt buộc", groups = OnCreate.class)
  @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự", groups = OnCreate.class)
  private String password;

  @NotNull(message = "Xác nhận mật khẩu là bắt buộc", groups = OnCreate.class)
  private String confirmPassword;

  @AssertTrue(message = "Mật khẩu xác nhận không khớp", groups = OnCreate.class)
  public boolean isValidConfirmPassword() {
    return !StringUtils.hasLength(password) || password.equals(confirmPassword);
  }
}