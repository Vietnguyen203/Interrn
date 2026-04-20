package com.manager.account.adapters.controllers;

import com.manager.account.application.usecase.PasswordResetService;
import com.manager.account.application.usecase.UserService;
import com.manager.account.application.dto.UserDTOs;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthAPI {

        private final UserService userService;
        private final PasswordResetService passwordResetService;

        // ─── Login ────────────────────────────────────────────────────────────────

        @PostMapping("/login")
        public BaseResponseDTO login(@RequestBody @Valid UserDTOs.LoginRequestDTO req) {
                log.info("Login request for empId: {}, server: {}", req.getEmployeeId(), req.getServer());
                return userService.login(req);
        }

        // ─── Forgot Password ──────────────────────────────────────────────────────

        /**
         * Bước 1: Gửi OTP 6 số về email đã đăng ký.
         * Body: { "email": "user@example.com" }
         */
        @PostMapping("/forgot-password")
        public BaseResponseDTO forgotPassword(@RequestBody @Valid UserDTOs.ForgotPasswordRequestDTO req) {
                return passwordResetService.sendOtp(req.getEmail());
        }

        /**
         * Bước 2: Xác nhận OTP và đặt lại mật khẩu mới.
         * Body: { "email": "...", "otp": "123456", "new_password": "..." }
         */
        @PostMapping("/reset-password")
        public BaseResponseDTO resetPassword(@RequestBody @Valid UserDTOs.VerifyOtpRequestDTO req) {
                return passwordResetService.verifyOtpAndReset(req.getEmail(), req.getOtp(),
                                req.getNewPassword());
        }
}
