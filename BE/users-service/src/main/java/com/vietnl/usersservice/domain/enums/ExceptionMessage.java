package com.vietnl.usersservice.domain.enums;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
    USER_NOT_FOUND("Người dùng không tồn tại"),
    USER_DUPLICATE("Tên đăng nhập đã tồn tại"),
    MISSING_REQUIRED_FIELD("Thiếu trường thông tin bắt buộc: %s"),
    INVALID_PASSWORD("Mật khẩu không hợp lệ"),
    WEAK_PASSWORD("Mật khẩu phải chứa ít nhất 1 kí tự viết hoa, 1 chữ số và 1 kí tự đặc biệt"),
    INVALID_CITIZEN_PID("Citizen PID phải từ 9 đến 12 kí tự"),
    OTP_INVALID("Mã OTP không hợp lệ hoặc đã qua sử dụng"),
    OTP_EXPIRED("Mã OTP đã hết hạn");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }
}
