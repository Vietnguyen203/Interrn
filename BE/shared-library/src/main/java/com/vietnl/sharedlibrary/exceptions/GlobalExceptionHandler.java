package com.vietnl.sharedlibrary.exceptions;

import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.CommunicationResponseException;
import com.eps.shared.core.exception.ErrorResponse;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.core.utils.*;
import com.eps.shared.core.utils.KeywordReplacer;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.setMessageCode(CommonErrorMessage.MISSING_PARAMETER);

    String paramName = ex.getParameterName();
    String message = String.format("Thiếu tham số bắt buộc '%s'", paramName);
    response.setMessage(message);
    response.setPath(request.getRequestURI());
    response.setTraceId(getTraceId());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String paramName = ex.getName();
    String value = ex.getValue() != null ? ex.getValue().toString() : "null";

    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.setMessageCode(CommonErrorMessage.INVALID_PARAMETER);

    String message = String.format("Giá trị '%s' không hợp lệ cho tham số '%s'", value, paramName);
    response.setMessage(message);
    response.setPath(request.getRequestURI());
    response.setTraceId(getTraceId());

    return ResponseEntity.badRequest().body(response);
  }

  // Handle validation
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    BindingResult bindingResult = ex.getBindingResult();

    List<ErrorResponse.FieldErrorDetail> fieldErrors =
        bindingResult.getFieldErrors().stream()
            .map(
                fieldError ->
                    new ErrorResponse.FieldErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
            .collect(Collectors.toList());

    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.setMessageCode(CommonErrorMessage.VALIDATION_FAILED);
    if (!fieldErrors.isEmpty()) {
      response.setMessage(fieldErrors.get(0).getMessage());
    }
    response.setPath(request.getRequestURI());
    response.setErrors(fieldErrors);
    response.setTraceId(getTraceId());

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintErrors(
      ConstraintViolationException ex, HttpServletRequest request) {
    Set<ConstraintViolation<?>> bindingResult = ex.getConstraintViolations();

    List<ErrorResponse.FieldErrorDetail> fieldErrors =
        bindingResult.stream()
            .map(
                fieldError ->
                    new ErrorResponse.FieldErrorDetail(
                        fieldError.getPropertyPath().toString(),
                        fieldError.getMessage(),
                        fieldError.getInvalidValue()))
            .collect(Collectors.toList());

    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.setMessageCode(CommonErrorMessage.VALIDATION_FAILED);
    if (!fieldErrors.isEmpty()) {
      response.setMessage(fieldErrors.get(0).getMessage());
    }
    response.setPath(request.getRequestURI());
    response.setErrors(fieldErrors);
    response.setTraceId(getTraceId());

    return ResponseEntity.badRequest().body(response);
  }

  // Handle bussiness error
  @ExceptionHandler(ResponseException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      ResponseException ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatusCode(ex.getStatusCode());
    response.setStatus(response.getStatusCode().value());
    response.setMessageCode(ex.getMessageCode());
    response.setMessage(ex.getMessage());
    response.setPath(request.getRequestURI());
    response.setTraceId(getTraceId());

    return ResponseEntity.status(ex.getStatusCode()).body(response);
  }

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<ErrorResponse> handleFeignException(
      FeignException ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatusCode(HttpStatus.valueOf(ex.status()));
    response.setStatus(ex.status());
    response.setMessageCode(CommonErrorMessage.FEIGN_ERROR);
    response.setMessage(ex.getMessage());
    response.setPath(request.getRequestURI());

    //    elkService.whiteLogException(getTraceId(), response, request);
    response.setTraceId(getTraceId());

    return ResponseEntity.status(ex.status()).body(response);
  }

  @ExceptionHandler(CommunicationResponseException.class)
  public ResponseEntity<?> handleCommunicationException(
      CommunicationResponseException ex, HttpServletRequest request) {

    Map<String, Object> body = ex.getBody();
    if (body.get("messageCode") == null) {
      ErrorResponse response = new ErrorResponse();
      response = new ErrorResponse();
      response.setTimestamp(LocalDateTime.now());
      response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      response.setMessageCode(CommonErrorMessage.FEIGN_ERROR);
      response.setMessage(ex.getMessage());
      response.setPath(request.getRequestURI());

      //      elkService.whiteLogException(getTraceId(), response, request);
      response.setTraceId(getTraceId());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    return ResponseEntity.status((Integer) body.get("status")).body(body);
  }

  //   Handle general error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setStatus(response.getStatusCode().value());
    response.setMessageCode(CommonErrorMessage.INTERNAL_SERVER);
    response.setMessage(ex.getMessage());
    response.setPath(request.getRequestURI());

    //    elkService.whiteLogException(
    //        getTraceId(),
    //        response.getMessage(),
    //        response.getTimestamp(),
    //        response.getPath(),
    //        response.getStatus(),
    //        request.getParameterMap(),
    //        HttpUtils.getHeaderMap(request));

    response.setMessage(response.getMessageCode().val());
    response.setTraceId(getTraceId());
    return ResponseEntity.internalServerError().body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
      NoResourceFoundException ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatusCode(HttpStatus.NOT_FOUND);
    response.setStatus(response.getStatus());
    response.setMessageCode(CommonErrorMessage.NO_RESOURCE_NOT_FOUND);
    response.setMessage(response.getMessageCode().val());
    response.setPath(request.getRequestURI());

    response.setTraceId(getTraceId());

    return ResponseEntity.status(404).body(response);
  }

  // Handle sort field
  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      PropertyReferenceException ex, HttpServletRequest request) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatusCode(HttpStatus.BAD_REQUEST);
    response.setStatus(response.getStatusCode().value());
    response.setMessageCode(CommonErrorMessage.FIELD_CANT_SORT);

    response.setMessage(
        KeywordReplacer.replaceKeywords(
            CommonErrorMessage.FIELD_CANT_SORT.val(),
            new HashMap<>() {
              {
                put("fieldname", ex.getPropertyName());
              }
            }));
    response.setPath(request.getRequestURI());

    response.setTraceId(getTraceId());

    return ResponseEntity.badRequest().body(response);
  }

  public String getTraceId() {
    return MDC.get(HeaderKeys.TRACE_ID);
  }
}
