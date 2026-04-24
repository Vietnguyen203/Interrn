package com.vietnl.usersservice.application.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vietnl.usersservice.domain.enums.UserRole;
import com.vietnl.usersservice.domain.enums.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserRequest {
    private UUID id;

    private String username;

    private UserRole role;
    private UserStatus status;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "\\d{10,11}", message = "Số điện thoại không quá 10 kí tự ")
    private String phoneNumber;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 255, message = "Tên không được vượt 255 kí tự ")
    private String fullName;

    @Email(message = "Sai định dạng email")
    @Size(max = 255, message = "Email không vượt quá 50 kí tự")
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Past(message = "Ngày sinh phải ở trong quá khứ")
    private LocalDateTime birthday;

    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "\\d{12}", message = "Căn cước công dân không phải đủ 12 kí tự")
    private String citizenPid;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số")
    private String password;
}
