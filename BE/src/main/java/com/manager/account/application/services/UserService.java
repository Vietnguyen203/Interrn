package com.manager.account.application.services;

import com.manager.account.infrastructure.config.ResponseCode;
import com.manager.account.interfaces.rest.dto.BaseResponseDTO;
import com.manager.account.interfaces.rest.dto.UserDTOs;
import com.manager.account.domain.models.entities.Users;
import com.manager.account.infrastructure.persistence.jpa.UserRepository;
import com.manager.account.infrastructure.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /* ========== REGISTER ========== */
    public BaseResponseDTO register(UserDTOs.RegisterRequestDTO req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            return fail("Username đã tồn tại");
        }
        Users u = new Users();
        u.setId(java.util.UUID.randomUUID().toString());
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setPhoneNumber(req.getPhoneNumber());

        u.setEmployeeId(notBlank(req.getEmployeeId()) ? req.getEmployeeId() : req.getUsername());
        u.setRole(notBlank(req.getRole()) ? req.getRole() : "WAITER");
        u.setServer(notBlank(req.getServer()) ? req.getServer() : "HCM");
        if (notBlank(req.getGender()))
            u.setGender(req.getGender());

        try {
            if (notBlank(req.getBirthday())) {
                u.setBirthday(LocalDate.parse(req.getBirthday(), DateTimeFormatter.ISO_DATE));
            }
        } catch (Exception ignored) {
        }

        try {
            userRepository.save(u);
            return ok("Đăng ký thành công", null);
        } catch (DuplicateKeyException e) {
            return fail("Trùng khoá (email/username).");
        } catch (Exception e) {
            return fail("Lỗi đăng ký: " + e.getMessage());
        }
    }

    /* ========== LOGIN ========== */
    public BaseResponseDTO login(UserDTOs.LoginRequestDTO login) throws UsernameNotFoundException {
        String principal = notBlank(login.getUsername()) ? login.getUsername()
                : notBlank(login.getEmployeeId()) ? login.getEmployeeId()
                        : null;
        if (principal == null) {
            return fail("Thiếu username hoặc employeeId");
        }

        // Linh hoạt: Tìm theo username trước, nếu không thấy tìm theo employeeId
        Users user = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmployeeId(principal))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + principal));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            return fail("Sai mật khẩu");
        }

        String finalServer = notBlank(user.getServer()) ? user.getServer()
                : notBlank(login.getServer()) ? login.getServer()
                        : "HCM";

        Map<String, Object> claims = new HashMap<>();
        String uid = notBlank(user.getUsername()) ? user.getUsername()
                : notBlank(user.getEmployeeId()) ? user.getEmployeeId()
                        : principal;
        claims.put("uid", uid);
        claims.put("role", notBlank(user.getRole()) ? user.getRole() : "USER");
        claims.put("server", finalServer);

        String token = jwtUtil.generateToken(uid, claims);

        return ok("Đăng nhập thành công", new UserDTOs.LoginResponseDTO(token));
    }

    /**
     * Lưu thông tin thiết bị (tạm thời ghi log).
     */
    public BaseResponseDTO save(Object user, UserDTOs.UpdateUserDeviceDTO request) {
        BaseResponseDTO response = new BaseResponseDTO();
        try {
            log.info("Saving user device. token={}",
                    request != null ? request.getToken() : "null");
            response.setMessage("OK");
        } catch (Exception e) {
            log.error("Save user device failed", e);
            response = new BaseResponseDTO(ResponseCode.UPDATE_ERROR.getCode(), "Save user device failed");
        }
        return response;
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng.
     */
    public BaseResponseDTO updateProfile(String username, UserDTOs.ProfileDTO dto) throws UsernameNotFoundException {
        BaseResponseDTO response = new BaseResponseDTO();
        try {
            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            if (dto.getFullName() != null)
                user.setFullName(dto.getFullName());
            if (dto.getPhoneNumber() != null)
                user.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getBirthday() != null) {
                try {
                    user.setBirthday(LocalDate.parse(dto.getBirthday(), DateTimeFormatter.ISO_DATE));
                } catch (Exception e) {
                    log.error("Invalid birthday format: {}", dto.getBirthday());
                }
            }
            if (dto.getAvatar() != null) {
                log.info("Avatar update requested");
            }

            if (user != null) {
                userRepository.save(user);
            }
            response.setCode("OK");
            response.setMessage("Updated successfully");
        } catch (Exception e) {
            log.error("Update profile failed for username={}", username, e);
            response = new BaseResponseDTO(ResponseCode.UPDATE_ERROR.getCode(), "Update profile failed");
        }
        return response;
    }

    /**
     * Cập nhật thông tin nhân viên (dành cho Admin).
     */
    public BaseResponseDTO updateEmployee(String server, String employeeId, UserDTOs.UpdateStaffRequestDTO dto) {
        try {
            Users user = userRepository.findByEmployeeIdAndServer(employeeId, server)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Staff not found: server=" + server + ", id=" + employeeId));

            if (notBlank(dto.getFullName()))
                user.setFullName(dto.getFullName());
            if (notBlank(dto.getRole()))
                user.setRole(dto.getRole());
            if (notBlank(dto.getEmail()))
                user.setEmail(dto.getEmail());
            if (notBlank(dto.getPhoneNumber()))
                user.setPhoneNumber(dto.getPhoneNumber());
            if (notBlank(dto.getGender()))
                user.setGender(dto.getGender());
            if (notBlank(dto.getPassword()))
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            if (notBlank(dto.getBirthday())) {
                try {
                    user.setBirthday(LocalDate.parse(dto.getBirthday(), DateTimeFormatter.ISO_DATE));
                } catch (Exception e) {
                    log.error("Invalid birthday format: {}", dto.getBirthday());
                }
            }

            if (user != null) {
                userRepository.save(user);
            }
            return ok("Updated employee successfully", null);
        } catch (Exception e) {
            log.error("Update employee failed", e);
            return fail("Update employee failed: " + e.getMessage());
        }
    }

    /**
     * Xoá nhân viên.
     */
    public BaseResponseDTO deleteEmployee(String server, String employeeId) {
        try {
            Users user = userRepository.findByEmployeeIdAndServer(employeeId, server)
                    .orElseThrow(() -> new UsernameNotFoundException("Staff not found"));

            if (user != null) {
                userRepository.delete(user);
            }
            return ok("Deleted employee successfully", null);
        } catch (Exception e) {
            log.error("Delete employee failed", e);
            return fail("Delete employee failed: " + e.getMessage());
        }
    }

    /**
     * Lấy URL/avatar của người dùng.
     */
    public BaseResponseDTO getAvatarUser(String username) throws UsernameNotFoundException {
        BaseResponseDTO response = new BaseResponseDTO();
        try {
            userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            response.setData(null);
            response.setMessage("Success");
        } catch (Exception e) {
            log.error("Get avatar failed for username={}", username, e);
            response = new BaseResponseDTO(ResponseCode.UPDATE_ERROR.getCode(), "Get avatar failed");
        }
        return response;
    }

    /* ===== Helpers ===== */
    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private BaseResponseDTO ok(String msg, Object data) {
        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("OK");
        res.setMessage(msg);
        res.setData(data);
        return res;
    }

    private BaseResponseDTO fail(String msg) {
        BaseResponseDTO res = new BaseResponseDTO();
        res.setCode("ERROR");
        res.setMessage(msg);
        return res;
    }
}
