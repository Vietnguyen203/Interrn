package com.manager.common.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Helpers ----------
    private ResponseEntity<Map<String, Object>> build(
            HttpStatus status, String message, HttpServletRequest req, Map<String, ?> errors) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status.value());
        body.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        if (req != null) {
            body.put("path", req.getRequestURI());
        }
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(status).body(body);
    }

    // ---------- 400: @Valid trên body (field errors) ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, errors);
    }

    // ---------- 400: JSON sai định dạng / thiếu trường bắt buộc ----------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", req, null);
    }

    // ---------- 400: @Validated trên query/path param ----------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(
            ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String key = String.valueOf(v.getPropertyPath());
            errors.put(key, v.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, errors);
    }

    // ---------- 400: thiếu param/path variable ----------
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class
    })
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Missing required parameter", req, null);
    }

    // ---------- 401: sai tài khoản/mật khẩu khi /users/login ----------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid employeeId/password", req, null);
    }

    // ---------- 403: không đủ quyền ----------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Access denied", req, null);
    }

    // ---------- 500: lỗi còn lại ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(
            Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req, null);
    }
}




