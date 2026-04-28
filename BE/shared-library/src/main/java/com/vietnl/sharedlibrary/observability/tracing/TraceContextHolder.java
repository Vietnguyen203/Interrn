package com.vietnl.sharedlibrary.observability.tracing; // package

// com.example.demo.shared.observability.tracing;

// import io.micrometer.tracing.Span;
// import io.micrometer.tracing.Tracer;
public class TraceContextHolder {

  //  private final Tracer tracer;

  private static final ThreadLocal<Integer> spanIdContext = new ThreadLocal<>();
  private static final ThreadLocal<String> traceIdContext = new ThreadLocal<>();

  public static String getCurrentTraceId() {
    return traceIdContext.get();
  }

  public static void setTraceId(String traceId) {

    traceIdContext.set(traceId);
  }

  /** Retrieves the current Span ID. */
  public static Integer getCurrentSpanId() {
    Integer spanId = spanIdContext.get();

    if (spanId == null) {
      spanId = 0;
    }

    spanIdContext.set(spanId + 1);

    return spanIdContext.get();
  }

  public static void clear() {
    spanIdContext.remove();
    traceIdContext.remove();
  }

  /** Retrieves the current Trace ID. */
  //  public String getCurrentTraceId() {
  //    Span currentSpan = tracer.currentSpan();
  //    return (currentSpan != null) ? currentSpan.context().traceId() : "N/A";
  //  }
  //
  //  /**
  //   * Retrieves the current Span ID.
  //   */
  //  public String getCurrentSpanId() {
  //    Span currentSpan = tracer.currentSpan();
  //    return (currentSpan != null) ? currentSpan.context().spanId() : "N/A";
  //  }
}
