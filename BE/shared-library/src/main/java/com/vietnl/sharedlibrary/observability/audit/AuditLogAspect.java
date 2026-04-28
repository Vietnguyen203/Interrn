package com.vietnl.sharedlibrary.observability.audit;

import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.core.json.JsonParserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

  private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();
  private static final DefaultParameterNameDiscoverer PARAM_NAME_DISCOVERER =
      new DefaultParameterNameDiscoverer();
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%(\\w+(?:\\.\\w+)*)%");

  private final Optional<AuditLogPort> auditLogPort;
  private final ObjectProvider<HttpServletRequest> requestProvider;

  @Autowired
  public AuditLogAspect(
      Optional<AuditLogPort> auditLogPort, ObjectProvider<HttpServletRequest> requestProvider) {
    this.auditLogPort = auditLogPort;
    this.requestProvider = requestProvider;
  }

  /**
   * Aspect chính: bắt các method có annotation @AuditLog, ghi lại thông tin audit log bao gồm
   * input, output, thời gian thực thi và kết quả (thành công/thất bại).
   */
  @Around("@annotation(auditLog)")
  public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
    if (auditLogPort.isEmpty()) {
      return joinPoint.proceed();
    }

    EvaluationContext spelContext = buildSpelContext(joinPoint);
    String inputData = resolveInputData(spelContext, auditLog);

    LocalDateTime startTime = LocalDateTime.now();
    Object result = null;
    boolean success = true;

    try {
      result = joinPoint.proceed();
      return result;
    } catch (Throwable ex) {
      success = false;
      throw ex;
    } finally {
      String outputData = resolveOutputData(success, result);

      PayloadAuditLogEvent data =
          PayloadAuditLogEvent.builder()
              .inputData(inputData)
              .outputData(outputData)
              .context(resolveContext())
              .build();

      sendAuditLog(spelContext, auditLog, startTime, LocalDateTime.now(), success, data);
    }
  }

  /** Resolve dữ liệu input từ SpEL expression được khai báo trong annotation @AuditLog. */
  private String resolveInputData(EvaluationContext context, AuditLog auditLog) {
    if (auditLog.input().isBlank()) {
      return null;
    }
    try {
      Object rawInput = resolveSpel(context, auditLog.input());
      return toJson(rawInput);
    } catch (Exception e) {
      log.warn(
          "AuditLogAspect: could not resolve input '{}': {}", auditLog.input(), e.getMessage());
      return null;
    }
  }

  /** Serialize output của method thành JSON nếu method thành công. */
  private String resolveOutputData(boolean success, Object result) {
    if (!success || result == null) {
      return null;
    }
    try {
      return toJson(result);
    } catch (Exception e) {
      log.warn("AuditLogAspect: could not serialize output: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Resolve một hoặc nhiều SpEL expression (phân cách bằng dấu phẩy). Nếu có 1 expression → trả về
   * giá trị đơn. Nếu nhiều → trả về Map.
   */
  private Object resolveSpel(EvaluationContext context, String inputExpression) {
    String[] expressions = inputExpression.split(",");

    if (expressions.length == 1) {
      return evalExpression(expressions[0].trim(), context);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    for (String expr : expressions) {
      String trimmed = expr.trim();
      String key = trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
      result.put(key, evalExpression(trimmed, context));
    }
    return result;
  }

  /** Evaluate một SpEL expression đơn lẻ, trả về null nếu lỗi. */
  private Object evalExpression(String expression, EvaluationContext context) {
    try {
      return SPEL_PARSER.parseExpression(expression).getValue(context);
    } catch (Exception e) {
      log.warn("AuditLogAspect: failed to evaluate '{}': {}", expression, e.getMessage());
      return null;
    }
  }

  /** Xây dựng SpEL EvaluationContext từ tên và giá trị tham số của method đang được gọi. */
  private EvaluationContext buildSpelContext(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();
    String[] paramNames = PARAM_NAME_DISCOVERER.getParameterNames(method);

    StandardEvaluationContext context = new StandardEvaluationContext();
    if (paramNames != null) {
      for (int i = 0; i < paramNames.length; i++) {
        context.setVariable(paramNames[i], i < args.length ? args[i] : null);
      }
    } else {
      log.warn(
          "AuditLogAspect: could not discover param names for '{}'. "
              + "Ensure compiled with -parameters flag.",
          method.getName());
    }
    return context;
  }

  /** Chuyển đổi object thành chuỗi JSON, trả về null nếu lỗi. */
  private String toJson(Object obj) {
    if (obj == null) {
      return null;
    }
    try {
      return JsonParserUtils.toJson(obj);
    } catch (Exception e) {
      log.warn(
          "AuditLogAspect: could not serialize [{}]: {}",
          obj.getClass().getSimpleName(),
          e.getMessage());
      return null;
    }
  }

  /** Xây dựng AuditLogEvent và gửi qua AuditLogPort. */
  private void sendAuditLog(
      EvaluationContext spelContext,
      AuditLog auditLog,
      LocalDateTime startTime,
      LocalDateTime endTime,
      boolean success,
      PayloadAuditLogEvent data) {
    try {
      AuditLogMetadata metadata = resolveMetadata(auditLog);
      if (metadata == null) {
        return;
      }

      String resolvedDescription = resolveDescription(metadata.getDescription(), spelContext);

      AuditLogEvent event =
          AuditLogEvent.builder()
              .resourceType(metadata.getResourceType())
              .action(metadata.getAction())
              .typeName(metadata.getTypeName())
              .description(resolvedDescription)
              .note(auditLog.note())
              .ipAddress(resolveIpAddress())
              .success(success)
              .startTime(startTime)
              .endTime(endTime)
              .duration(Duration.between(startTime, endTime).toMillis())
              .data(data)
              .build();

      auditLogPort.ifPresent(port -> port.log(event));

    } catch (Exception e) {
      log.error("AuditLogAspect: failed to send audit log: {}", e.getMessage(), e);
    }
  }

  /** Resolve AuditLogMetadata từ enum class và giá trị được khai báo trong annotation. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private AuditLogMetadata resolveMetadata(AuditLog auditLog) {
    try {
      Class<? extends Enum<?>> enumClass = auditLog.enumClass();

      if (!AuditLogMetadata.class.isAssignableFrom(enumClass)) {
        log.warn(
            "AuditLogAspect: enum [{}] must implement AuditLogMetadata, skipping",
            enumClass.getName());
        return null;
      }

      Enum<?> constant = Enum.valueOf((Class) enumClass, auditLog.value());
      return (AuditLogMetadata) constant;

    } catch (IllegalArgumentException e) {
      log.warn(
          "AuditLogAspect: enum constant [{}] not found in [{}], skipping",
          auditLog.value(),
          auditLog.enumClass().getName());
      return null;
    } catch (Exception e) {
      log.warn("AuditLogAspect: unexpected error resolving metadata: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Thay thế các placeholder %key% trong description bằng giá trị thực từ tham số method.
   *
   * @param template chuỗi description chứa placeholder, ví dụ: "Cập nhật %request.userCode%"
   * @param context SpEL context đã build sẵn từ tham số method (chứa #uuid, #request, ...)
   * @return chuỗi description đã thay thế placeholder bằng giá trị thực
   */
  private String resolveDescription(String template, EvaluationContext context) {
    if (template == null || !template.contains("%")) {
      return template;
    }
    try {
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
      StringBuilder result = new StringBuilder();
      while (matcher.find()) {
        String key = matcher.group(1);
        String spelExpr = "#" + key;
        try {
          Object value = SPEL_PARSER.parseExpression(spelExpr).getValue(context);
          matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
        } catch (Exception e) {
          log.warn("AuditLogAspect: could not resolve placeholder '%{}%': {}", key, e.getMessage());
          matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
        }
      }
      matcher.appendTail(result);
      return result.toString();
    } catch (Exception e) {
      log.warn("AuditLogAspect: failed to resolve description placeholders: {}", e.getMessage());
      return template;
    }
  }

  /** Lấy HeaderContext của user hiện tại từ MDC (Mapped Diagnostic Context). */
  private HeaderContext resolveContext() {
    String xUser = MDC.get(HeaderKeys.USER);
    if (xUser == null || xUser.isBlank()) {
      return null;
    }
    try {
      return JsonParserUtils.entity(xUser, HeaderContext.class);
    } catch (Exception e) {
      log.warn("AuditLogAspect: could not parse HeaderContext from MDC: {}", xUser, e);
      return null;
    }
  }

  /** Lấy địa chỉ IP client, ưu tiên X-Forwarded-For header, fallback về remoteAddr. */
  private String resolveIpAddress() {
    try {
      HttpServletRequest request = requestProvider.getIfAvailable();
      if (request == null) {
        return null;
      }

      String forwarded = request.getHeader("X-Forwarded-For");
      if (forwarded != null && !forwarded.isBlank()) {
        return forwarded.split(",")[0].trim();
      }
      return request.getRemoteAddr();
    } catch (Exception e) {
      log.warn("AuditLogAspect: could not resolve IP: {}", e.getMessage());
      return null;
    }
  }
}
