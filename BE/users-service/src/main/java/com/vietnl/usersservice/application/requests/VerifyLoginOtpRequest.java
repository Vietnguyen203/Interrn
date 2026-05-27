package com.vietnl.usersservice.application.requests;

import lombok.Data;

@Data
public class VerifyLoginOtpRequest {
    private String username;
    private String otp;
    private String deviceId;
    private Boolean rememberMe;
}
