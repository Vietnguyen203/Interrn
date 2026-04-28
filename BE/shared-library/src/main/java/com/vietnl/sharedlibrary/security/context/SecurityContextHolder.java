package com.vietnl.sharedlibrary.security.context;

import com.eps.shared.core.constants.HeaderKeys;
import com.eps.shared.core.context.HeaderContext;
import org.slf4j.MDC;

import java.util.function.Function;

public class SecurityContextHolder {

  private static final ThreadLocal<HeaderContext> CONTEXT = new ThreadLocal<>();
  private static final ThreadLocal<String> xUserCONTEXT = new ThreadLocal<>();

  private SecurityContextHolder() {}

  public static void set(String xUser) {
    MDC.put(HeaderKeys.USER, xUser);
    xUserCONTEXT.set(xUser);
    CONTEXT.set(new HeaderContext(xUser));
  }

  public static HeaderContext get() {
    return CONTEXT.get();
  }

  public static String getUserCode() {

    return extractClaims(HeaderContext::getUserCode);
  }

  public static <R> R extractClaims(Function<HeaderContext, R> extractResolver) {

    return extractResolver.apply(CONTEXT.get());
  }

  public static String getXUser() {
    return xUserCONTEXT.get();
  }

  public static void clear() {
    CONTEXT.remove();
    xUserCONTEXT.remove();
    MDC.remove(HeaderKeys.USER); // hoặc MDC.clear() nếu bạn set nhiều field
  }

  public static boolean isAuthenticated() {
    return CONTEXT.get().isAuthenticated();
  }
}
