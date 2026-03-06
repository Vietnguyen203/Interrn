package com.manager.account.interfaces.rest.controllers;

import com.manager.account.application.services.PasswordResetService;
import com.manager.account.application.services.UserService;
import com.manager.account.interfaces.rest.dto.UserDTOs;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.manager.account.interfaces.rest.dto.BaseResponseDTO;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

        private final UserService userService;
        private final PasswordResetService passwordResetService;

        // ─── Login ────────────────────────────────────────────────────────────────

        @PostMapping("/login")
        public BaseResponseDTO login(@RequestBody @Valid UserDTOs.LoginRequestDTO req) {
                try {
                        log.info("Login request for empId: {}, server: {}", req.getEmployeeId(), req.getServer());
                        return userService.login(req);
                } catch (UsernameNotFoundException e) {
                        return new BaseResponseDTO("ERROR", e.getMessage());
                } catch (Exception e) {
                        return new BaseResponseDTO("ERROR", "Lỗi đăng nhập: " + e.getMessage());
                }
        }

        // ─── Forgot Password ──────────────────────────────────────────────────────

        /**
         * Bước 1: Gửi OTP 6 số về email đã đăng ký.
         * Body: { "email": "user@example.com" }
         */
        @PostMapping("/forgot-password")
        public BaseResponseDTO forgotPassword(@RequestBody @Valid UserDTOs.ForgotPasswordRequestDTO req) {
                try {
                        return passwordResetService.sendOtp(req.getEmail());
                } catch (Exception e) {
                        log.error("Forgot password error: {}", e.getMessage());
                        return new BaseResponseDTO("ERROR", "Có lỗi xảy ra, vui lòng thử lại");
                }
        }

        /**
         * Bước 2: Xác nhận OTP và đặt lại mật khẩu mới.
         * Body: { "email": "...", "otp": "123456", "new_password": "..." }
         */
        @PostMapping("/reset-password")
        public BaseResponseDTO resetPassword(@RequestBody @Valid UserDTOs.VerifyOtpRequestDTO req) {
                try {
                        return passwordResetService.verifyOtpAndReset(req.getEmail(), req.getOtp(),
                                        req.getNewPassword());
                } catch (Exception e) {
                        log.error("Reset password error: {}", e.getMessage());
                        return new BaseResponseDTO("ERROR", "Có lỗi xảy ra, vui lòng thử lại");
                }
        }
}
