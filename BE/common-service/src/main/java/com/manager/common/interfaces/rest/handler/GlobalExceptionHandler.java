package com.manager.common.interfaces.rest.handler;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("Username not found: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", "Tài khoản không tồn tại");
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", "Mật khẩu không chính xác");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", "Bạn không có quyền thực hiện hành động này");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation error: {}", errors);
        String firstError = errors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ");
        return new BaseResponseDTO("ERROR", firstError, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", "Vi phạm ràng buộc dữ liệu: " + ex.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class
    })
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleBadRequest(Exception ex) {
        log.error("Bad request: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", "Yêu cầu không hợp lệ hoặc thiếu tham số");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return new BaseResponseDTO("ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponseDTO handleAllExceptions(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return new BaseResponseDTO("ERROR", "Hệ thống gặp sự cố: " + ex.getMessage());
    }
}
