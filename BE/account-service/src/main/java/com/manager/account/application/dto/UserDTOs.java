package com.manager.account.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

public class UserDTOs {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequestDTO {
        private String username;
        @JsonAlias({ "employee_id", "employeeId" })
        private String employeeId;
        @NotBlank
        private String password;
        @JsonAlias({ "server_name", "server" })
        private String server;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponseDTO {
        private String token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisterRequestDTO {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        @NotBlank(message = "Họ và tên không được để trống")
        @JsonProperty("full_name")
        @JsonAlias("fullName")
        private String fullName;

        private String email;

        @JsonProperty("phone_number")
        @JsonAlias("phoneNumber")
        private String phoneNumber; // optional

        private String server;
        private String role;

        @JsonProperty("employee_id")
        @JsonAlias({ "employeeId", "uid" })
        private String employeeId;

        private String birthday; // optional
        private String gender; // optional: MALE | FEMALE | OTHER
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDTO {
        private String username;
        private String fullName;
        private String password;
        private String email;
        private String phoneNumber;
        private String birthday;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserListItemDTO {
        @JsonProperty("uid")
        private String employeeId;
        @JsonProperty("fullName")
        private String displayName;
        private String role;
        @JsonProperty("phoneNumber")
        private String phoneNumber;
        @JsonProperty("birthday")
        private String birthday;
        @JsonProperty("createdDate")
        private String createdAt;
        @JsonProperty("createdBy")
        private String createdBy;
        @JsonProperty("email")
        private String email;
        @JsonProperty("server")
        private String server;
        @JsonProperty("gender")
        private String gender;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateStaffRequestDTO {
        @JsonProperty("full_name")
        @JsonAlias("fullName")
        private String fullName;

        private String role;
        private String password;
        private String email;

        @JsonProperty("phone_number")
        @JsonAlias("phoneNumber")
        private String phoneNumber;

        private String birthday;
        private String gender; // optional: MALE | FEMALE | OTHER
    }

    @Data
    public static class ProfileDTO {
        @NotBlank(message = "Họ và tên không được để trống")
        @ApiModelProperty(notes = "fullName", example = "Nguyen Van A", required = true)
        @JsonProperty("full_name")
        @JsonAlias("fullName")
        private String fullName;

        @NotBlank(message = "Số điện thoại không được để trống")
        @ApiModelProperty(notes = "phoneNumber", example = "0123456789", required = true)
        @JsonProperty("phone_number")
        @JsonAlias("phoneNumber")
        private String phoneNumber;
        @ApiModelProperty(notes = "avatar", example = "http://example.com/avatar.png", required = false)
        private String avatar;
        @NotBlank(message = "Ngày sinh không được để trống")
        @ApiModelProperty(notes = "birthday", example = "2000-01-01", required = true)
        private String birthday;
        @ApiModelProperty(notes = "password", example = "newpassword123", required = false)
        private String password;
        @ApiModelProperty(notes = "numberFollowing", example = "10", required = false)
        private int numberFollowing = -1;
        @ApiModelProperty(notes = "numberFollower", example = "5", required = false)
        private int numberFollower = -1;
        @ApiModelProperty(notes = "numberLikes", example = "100", required = false)
        private int numberLikes = -1;
    }

    @Data
    public static class UpdateUserDeviceDTO {
        private String token;
        private String platform;
    }

    @Data
    public static class ChangePasswordRequestDTO {
        private String oldPassword;
        private String newPassword;
    }

    @Data
    public static class ResetPasswordRequestDTO {
        private String email;
    }

    // ─── Forgot Password ───────────────────────────────────────────────────────

    /** Bước 1: Gửi OTP về email */
    @Data
    public static class ForgotPasswordRequestDTO {
        @NotBlank(message = "Email không được để trống")
        private String email;
    }

    /** Bước 2: Xác nhận OTP và đặt mật khẩu mới */
    @Data
    public static class VerifyOtpRequestDTO {
        @NotBlank(message = "Email không được để trống")
        private String email;

        @NotBlank(message = "OTP không được để trống")
        private String otp;

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @com.fasterxml.jackson.annotation.JsonProperty("new_password")
        private String newPassword;
    }
}
