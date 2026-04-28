package com.vietnl.sharedlibrary.observability.logging;

/**
 * Enumeration of log event types.
 *
 * <p>Each event type defines a category of structured log entry. All types share the common
 * envelope fields (event, message, userId, fullname, duration, error). Type-specific payload fields
 * are added via {@link LogEntry.Builder#put(String, Object)}.
 */
public enum LogEventType {

  /** HTTP request/response lifecycle events. Includes request and response payload. */
  REQUEST,

  /** Scheduled job execution events. Includes jobName, triggerType, executionResult. */
  SCHEDULE,

  /** Audit trail entries for user actions. Includes action, resource, before, after. */
  AUDIT_LOG,

  /** Generic application events with custom payload. */
  EVENT
}
