package com.manager.account.infrastructure.config;

import lombok.Getter;

/**
 * Enum chứa các mã phản hồi chuẩn của hệ thống.
 * Vừa có code, vừa có message mặc định.
 */
@Getter
public enum ResponseCode {

    SUCCESS("00", "SUCCESS"),
    USER_INVALID("SD001", "User or password invalid"),
    USER_INVALID_V2("SD002", "Registration failed"),
    PASS_USED("SD003", "Password has been used"),
    UPDATE_ERROR("UP001", "Update failed"),
    VIDEO_GET_ERROR("VD001", "Cannot get video"),
    UNKNOWN("MG999", "Unknown error");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}




