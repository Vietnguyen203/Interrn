package com.manager.account.application.services;

import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Xử lý toàn bộ flow Forgot Password:
 * 1. sendOtp(email) → tạo OTP 6 số, lưu in-memory, gửi email
 * 2. verifyOtpAndReset(...) → kiểm tra OTP + TTL, đổi mật khẩu, xoá OTP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.ttl-seconds:300}")
    private long otpTtlSeconds;

    @Value("${spring.mail.username:no-reply@example.com}")
    private String fromEmail;

    // ── OTP store (in-memory) ──────────────────────────────────────────────────
    // Key = email (lowercase), Value = OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Bước 1: Tạo và gửi OTP về email đã đăng ký.
     */
    public BaseResponseDTO sendOtp(String email) {
        if (email == null || email.isBlank()) {
            return fail("Email không được để trống");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<Users> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty()) {
            // Trả về thông báo chung để tránh lộ thông tin tài khoản
            log.warn("Forgot password requested for non-existent email: {}", normalizedEmail);
            return ok("Nếu email tồn tại trong hệ thống, OTP sẽ được gửi.");
        }

        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusSeconds(otpTtlSeconds);
        otpStore.put(normalizedEmail, new OtpEntry(otp, expiry));

        try {
            sendEmail(normalizedEmail, otp, userOpt.get().getFullName());
            log.info("OTP sent to email: {}", normalizedEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", normalizedEmail, e.getMessage());
            // Vẫn log OTP ra console để test trong dev (xoá khi production)
            log.warn("[DEV] OTP for {} = {}", normalizedEmail, otp);
        }

        return ok("Nếu email tồn tại trong hệ thống, OTP sẽ được gửi.");
    }

    /**
     * Bước 2: Xác nhận OTP và đặt lại mật khẩu mới.
     */
    @org.springframework.transaction.annotation.Transactional
    public BaseResponseDTO verifyOtpAndReset(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null) {
            return fail("Vui lòng điền đầy đủ thông tin");
        }
        if (newPassword.length() < 6) {
            return fail("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        String normalizedEmail = email.trim().toLowerCase();
        OtpEntry entry = otpStore.get(normalizedEmail);

        if (entry == null) {
            return fail("OTP không hợp lệ hoặc chưa được gửi");
        }

        if (LocalDateTime.now().isAfter(entry.expiry())) {
            otpStore.remove(normalizedEmail);
            return fail("OTP đã hết hạn. Vui lòng yêu cầu OTP mới");
        }

        if (!entry.otp().equals(otp.trim())) {
            return fail("OTP không chính xác");
        }

        // OTP hợp lệ → đổi mật khẩu
        Optional<Users> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isEmpty()) {
            return fail("Không tìm thấy tài khoản");
        }

        Users user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xoá OTP sau khi sử dụng
        otpStore.remove(normalizedEmail);
        log.info("Password reset successful for email: {}", normalizedEmail);

        return ok("Đặt lại mật khẩu thành công");
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateOtp() {
        // OTP 6 số ngẫu nhiên (có thể bắt đầu bằng 0)
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private void sendEmail(String toEmail, String otp, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Food Order] Mã OTP đặt lại mật khẩu");
        message.setText(
                "Xin chào " + (fullName != null ? fullName : "") + ",\n\n" +
                        "Mã OTP để đặt lại mật khẩu của bạn là:\n\n" +
                        "    " + otp + "\n\n" +
                        "Mã có hiệu lực trong " + (otpTtlSeconds / 60) + " phút.\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.\n\n" +
                        "Trân trọng,\nFood Order System");
        mailSender.send(message);
    }

    private BaseResponseDTO ok(String msg) {
        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("OK");
        res.setMessage(msg);
        return res;
    }

    private BaseResponseDTO fail(String msg) {
        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("ERROR");
        res.setMessage(msg);
        return res;
    }

    // ── Inner record ───────────────────────────────────────────────────────────

    /**
     * Lưu trữ OTP và thời điểm hết hạn.
     */
    private record OtpEntry(String otp, LocalDateTime expiry) {
    }
}
