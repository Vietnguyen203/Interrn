package com.vietnl.sharedlibrary.observability.logging;

import com.eps.shared.core.context.HeaderContext;
import com.eps.shared.security.context.SecurityContextHolder;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable structured log entry with a fluent builder API.
 *
 * <p>Produces a consistent JSON envelope for all event types. Common fields ({@code event}, {@code
 * message}, {@code userId}, {@code fullname}, {@code duration}, {@code error}) are shared.
 * Type-specific fields are added via {@link Builder#put(String, Object)}.
 *
 * <p>{@code userId} and {@code fullname} are auto-populated from MDC (set by {@link
 * SecurityContextHolder}) if not explicitly provided.
 *
 * <h3>Usage examples</h3>
 *
 * <pre>{@code
 * // HTTP event
 * LogEntry.http("<<< POST /api/users 400 (45ms)")
 *     .request(req -> req.method("POST").url("/api/users").ip("127.0.0.1").body("{...}"))
 *     .response(res -> res.statusCode(400).body("{\"error\":\"Validation failed\"}"))
 *     .duration(45)
 *     .error(exception)
 *     .build();
 *
 * // Audit log
 * LogEntry.audit("User role updated")
 *     .put("action", "UPDATE")
 *     .put("resource", "UserRole")
 *     .put("resourceId", "usr_456")
 *     .put("before", Map.of("role", "USER"))
 *     .put("after", Map.of("role", "ADMIN"))
 *     .build();
 *
 * // Schedule
 * LogEntry.schedule("Clean expired tokens completed")
 *     .put("jobName", "cleanExpiredTokens")
 *     .put("triggerType", "CRON")
 *     .put("executionResult", "SUCCESS")
 *     .duration(1200)
 *     .build();
 *
 * // Generic event
 * LogEntry.event("Payment processed")
 *     .put("orderId", "ORD-001")
 *     .put("amount", 500000)
 *     .put("paymentMethod", "MOMO")
 *     .build();
 * }</pre>
 */
@Getter
public final class LogEntry {

  private final Map<String, Object> fields;
  private final String message;

  @Getter(AccessLevel.NONE)
  private final Throwable throwable;

  public String getError() {

    return formatThrowable(throwable);
  }

  public boolean isError() {
    return throwable != null;
  }

  private static String safeMsg(String msg) {
    if (msg == null) {
      return "";
    }
    return msg.replaceAll("[\\r\\n]+", " ");
  }

  public static String formatThrowable(Throwable ex) {
    if (ex == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    // message chính
    sb.append(ex.getClass().getSimpleName()).append(": ").append(safeMsg(ex.getMessage()));

    // stacktrace dòng đầu tiên (nơi lỗi xảy ra)
    StackTraceElement[] stack = ex.getStackTrace();
    if (stack != null && stack.length > 0) {
      StackTraceElement top = stack[0];

      sb.append(" @ ")
          .append(top.getClassName())
          .append(".")
          .append(top.getMethodName())
          .append("(")
          .append(top.getFileName())
          .append(":")
          .append(top.getLineNumber())
          .append(")");
    }

    return sb.toString();
  }

  private LogEntry(Map<String, Object> fields, String message, Throwable throwable) {
    this.fields = fields;
    this.message = message;
    this.throwable = throwable;
  }

  /** Returns the structured fields map for Logstash Markers. */
  public Map<String, Object> fields() {
    return fields;
  }

  /** Returns the human-readable log message. */
  public String message() {
    return message;
  }

  /** Returns the associated throwable, or {@code null}. */
  public Throwable throwable() {
    return throwable;
  }

  // ── Factory methods ────────────────────────────────────────────────────────

  /** Create a builder for an {@link LogEventType#REQUEST} event. */
  public static Builder request(String message) {
    return new Builder(LogEventType.REQUEST, message);
  }

  /** Create a builder for a {@link LogEventType#SCHEDULE} event. */
  public static Builder schedule(String message) {
    return new Builder(LogEventType.SCHEDULE, message);
  }

  /** Create a builder for an {@link LogEventType#AUDIT_LOG} event. */
  public static Builder audit(String message) {
    return new Builder(LogEventType.AUDIT_LOG, message);
  }

  /** Create a builder for a {@link LogEventType#EVENT} event. */
  public static Builder event(String message) {
    return new Builder(LogEventType.EVENT, message);
  }

  // ── Builder ────────────────────────────────────────────────────────────────

  public static final class Builder {

    private final LogEventType eventType;
    private final String message;
    private final Map<String, Object> fields = new LinkedHashMap<>();
    private Throwable throwable;

    private Builder(LogEventType eventType, String message) {
      this.eventType = eventType;
      this.message = message;
    }

    /** Set duration in milliseconds. */
    public Builder duration(long durationMs) {
      fields.put("duration", durationMs);
      return this;
    }

    /** Attach an error/exception to this log entry. */
    public Builder error(Throwable t) {
      if (t != null) {
        throwable = t;
        var errorMap = new LinkedHashMap<String, String>();
        errorMap.put("type", t.getClass().getName());
        errorMap.put("message", t.getMessage());
        fields.put("error", errorMap);
      }
      return this;
    }

    /** Add HTTP request details (only meaningful for HTTP events). */
    public Builder request(java.util.function.Consumer<RequestInfo> configurer) {
      var info = new RequestInfo();
      configurer.accept(info);
      fields.put("request", info.toMap());
      return this;
    }

    /** Add HTTP response details (only meaningful for HTTP events). */
    public Builder response(java.util.function.Consumer<ResponseInfo> configurer) {
      var info = new ResponseInfo();
      configurer.accept(info);
      fields.put("response", info.toMap());
      return this;
    }

    /** Add any custom key-value field to the log entry. */
    public Builder put(String key, Object value) {
      if (value != null) {
        fields.put(key, value);
      }
      return this;
    }

    /** Build the immutable {@link LogEntry}. Auto-populates userId/fullname from context. */
    public LogEntry build() {
      var result = new LinkedHashMap<String, Object>();

      // Common envelope
      result.put("event", eventType.name());

      if (SecurityContextHolder.isAuthenticated()) {
        result.put(
            "context",
            new HashMap<>() {
              {
                put("userId", SecurityContextHolder.extractClaims(HeaderContext::getUserId));
                put("name", SecurityContextHolder.extractClaims(HeaderContext::getName));
                put("userCode", SecurityContextHolder.extractClaims(HeaderContext::getUserCode));
                put(
                    "applicationId",
                    SecurityContextHolder.extractClaims(HeaderContext::getApplicationCode));
              }
            });
      }

      // Event-specific fields
      {
        result.putAll(fields);
      }

      return new LogEntry(result, message, throwable);
    }
  }

  // ── Nested info objects ────────────────────────────────────────────────────

  /** Builder for HTTP request metadata. */
  public static final class RequestInfo {
    private final Map<String, Object> data = new LinkedHashMap<>();

    public RequestInfo method(String method) {
      data.put("method", method);
      return this;
    }

    public RequestInfo url(String url) {
      data.put("url", url);
      return this;
    }

    public RequestInfo ip(String ip) {
      data.put("ip", ip);
      return this;
    }

    public RequestInfo headers(Map<String, String> headers) {
      data.put("headers", headers);
      return this;
    }

    public RequestInfo body(String body) {
      if (body != null && !body.isBlank()) {
        data.put("body", body);
      }
      return this;
    }

    public RequestInfo params(Object params) {
      if (params != null) {
        data.put("params", params);
      }
      return this;
    }

    Map<String, Object> toMap() {
      return data;
    }
  }

  /** Builder for HTTP response metadata. */
  public static final class ResponseInfo {
    private final Map<String, Object> data = new LinkedHashMap<>();

    public ResponseInfo statusCode(int statusCode) {
      data.put("statusCode", statusCode);
      return this;
    }

    public ResponseInfo body(String body) {
      if (body != null && !body.isBlank()) {
        data.put("body", body);
      }
      return this;
    }

    Map<String, Object> toMap() {
      return data;
    }
  }
}
