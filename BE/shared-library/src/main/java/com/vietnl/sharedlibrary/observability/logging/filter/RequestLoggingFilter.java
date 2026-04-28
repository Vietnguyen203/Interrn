package com.vietnl.sharedlibrary.observability.logging.filter;

import com.eps.shared.core.constants.CommonConstant;
import com.eps.shared.core.json.JsonParserUtils;
import com.eps.shared.observability.logging.LogEntry;
import com.eps.shared.observability.logging.LoggingProperties;
import com.eps.shared.observability.logging.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@Order(CommonConstant.LOGGING_FILTER_ORDER)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

  private final LoggingProperties properties;
  private final LoggingService loggingService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getServletPath();

    if (properties.getWeb().isExcludedPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    LocalDateTime startTime = LocalDateTime.now();

    CachedBodyHttpServletRequestDecorator wrappedRequest =
        new CachedBodyHttpServletRequestDecorator(request, properties);

    CachedBodyHttpServletResponseDecorator wrappedResponse =
        new CachedBodyHttpServletResponseDecorator(
            response, properties, wrappedRequest.isShouldCapture());

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);

    } finally {
      Throwable throwable =
          (Throwable)
              request.getAttribute(
                  "org.springframework.boot.web.servlet.error.DefaultErrorAttributes.ERROR");
      logRequest(
          startTime,
          wrappedRequest.getMethod(),
          wrappedRequest.getServletPath(),
          resolveClientIp(wrappedRequest),
          wrappedRequest.getParameterMap(),
          wrappedRequest.getBody(),
          wrappedResponse.getStatus(),
          wrappedResponse.getBodyAsString(),
          throwable);
      wrappedResponse.copyBodyToResponse(); // cực kỳ quan trọng
    }
  }

  private void logRequest(
      LocalDateTime startTime,
      String method,
      String path,
      String ipAddress,
      Object params,
      String body,
      int statusCode,
      String responseBody,
      Throwable throwable) {

    long duration = Duration.between(startTime, LocalDateTime.now()).toMillis();

    String message = null;
    boolean isError = statusCode >= 400;

    LogEntry.Builder builder =
        LogEntry.request(message)
            .duration(duration)
            .request(
                req -> {
                  req.method(method).url(path).ip(ipAddress);
                  if (isError) {
                    req.body(truncateBody(body));
                    req.params(params);
                  }
                })
            .response(
                res -> {
                  res.statusCode(statusCode);
                  if (isError) {
                    try {
                      Map<String, Object> responseJson = JsonParserUtils.toObjectMap(responseBody);

                      res.body(
                          truncateBody(
                              String.format(
                                  "{\"messageCode\":\"%s\",\"message\":\"%s\"}",
                                  responseJson.get("messageCode"), responseJson.get("message"))));

                    } catch (Exception e) {
                      res.body(truncateBody(responseBody)); // fallback
                    }
                  }
                });

    if (throwable != null) {
      builder.error(throwable);
    }

    LogEntry entry = builder.build();

    loggingService.logger(entry);
  }

  private String truncateBody(String body) {
    if (body == null || body.isBlank()) {
      return null;
    }
    int maxBytes = properties.getWeb().getMaxBodyBytes();
    return body.length() > maxBytes ? body.substring(0, maxBytes) + "...[TRUNCATED]" : body;
  }

  private String resolveClientIp(CachedBodyHttpServletRequestDecorator request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isEmpty()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
  }
}

class CachedBodyHttpServletRequestDecorator extends HttpServletRequestWrapper {

  private byte[] cachedBody;
  @Getter private final boolean shouldCapture;
  private final int maxBodyBytes;
  private boolean bodyRead = false;

  public CachedBodyHttpServletRequestDecorator(
      HttpServletRequest request, LoggingProperties properties) {
    super(request);
    shouldCapture = properties.getWeb().isLoggableContentType(request.getContentType());
    maxBodyBytes = properties.getWeb().getMaxBodyBytes();
  }

  private void cacheInputStream() throws IOException {
    if (bodyRead) {
      return;
    }

    InputStream is = super.getInputStream();

    ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.min(1024, maxBodyBytes));

    byte[] data = new byte[1024];
    int totalRead = 0;
    int nRead;

    while ((nRead = is.read(data)) != -1) {
      int bytesToWrite = Math.min(nRead, maxBodyBytes - totalRead);
      if (bytesToWrite <= 0) {
        break;
      }

      buffer.write(data, 0, bytesToWrite);
      totalRead += bytesToWrite;

      if (totalRead >= maxBodyBytes) {
        break;
      }
    }

    cachedBody = buffer.toByteArray();
    bodyRead = true;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (!bodyRead) {
      cacheInputStream();
    }

    byte[] body = cachedBody != null ? cachedBody : new byte[0];

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

    return new ServletInputStream() {
      @Override
      public int read() {
        return byteArrayInputStream.read();
      }

      @Override
      public boolean isFinished() {
        return byteArrayInputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {}
    };
  }

  public String getBody() {
    if (!shouldCapture) {
      return "";
    }

    try {
      if (!bodyRead) {
        cacheInputStream();
      }
    } catch (IOException e) {
      return "";
    }

    return cachedBody == null || cachedBody.length == 0
        ? ""
        : new String(cachedBody, StandardCharsets.UTF_8);
  }
}

class CachedBodyHttpServletResponseDecorator extends HttpServletResponseWrapper {

  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

  private ServletOutputStream outputStream;
  private PrintWriter writer;

  private final boolean shouldCapture;
  private final int maxBodyBytes;
  private int currentSize = 0;

  public CachedBodyHttpServletResponseDecorator(
      HttpServletResponse response, LoggingProperties properties, boolean shouldCapture) {

    super(response);

    this.shouldCapture = shouldCapture;
    maxBodyBytes = properties.getWeb().getMaxBodyBytes();
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {

    // ❗ Không capture → trả stream gốc luôn
    if (!shouldCapture) {
      return super.getOutputStream();
    }

    if (outputStream == null) {
      outputStream =
          new ServletOutputStream() {

            @Override
            public void write(int b) throws IOException {

              if (currentSize < maxBodyBytes) {
                buffer.write(b);
                currentSize++;
              }

              // luôn ghi xuống response thật
              CachedBodyHttpServletResponseDecorator.super.getOutputStream().write(b);
            }

            @Override
            public boolean isReady() {
              return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {}
          };
    }

    return outputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {

    if (!shouldCapture) {
      return super.getWriter();
    }

    if (writer == null) {
      writer =
          new PrintWriter(new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8), true);
    }

    return writer;
  }

  public byte[] getBody() {
    return shouldCapture ? buffer.toByteArray() : new byte[0];
  }

  public String getBodyAsString() {
    if (!shouldCapture) {
      return null;
    }

    byte[] body = getBody();

    return body.length == 0 ? null : new String(body, StandardCharsets.UTF_8);
  }

  public void copyBodyToResponse() {
    // ❗ không cần nữa vì đã stream trực tiếp
  }
}
