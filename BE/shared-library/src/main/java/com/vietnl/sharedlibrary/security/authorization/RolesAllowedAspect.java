package com.vietnl.sharedlibrary.security.authorization;

import com.eps.shared.core.constants.SystemConstant;
import com.eps.shared.core.exception.CommonErrorMessage;
import com.eps.shared.core.exception.ResponseException;
import com.eps.shared.core.json.JsonParserUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class RolesAllowedAspect {

  @Around("@annotation(rolesAllowed)")
  public Object checkUserRoles(ProceedingJoinPoint joinPoint, RolesAllowed rolesAllowed)
      throws Throwable {

    validateAnnotation(rolesAllowed);

    Set<String> userRoles = extractUserRolesFromHeader();

    Set<String> allowedRoles =
        Arrays.stream(rolesAllowed.value())
            .filter(r -> r != null && !r.trim().isEmpty())
            .collect(Collectors.toSet());

    if (Collections.disjoint(userRoles, allowedRoles)) {
      throw new ResponseException(HttpStatus.FORBIDDEN, CommonErrorMessage.FORBIDDEN.val());
    }

    return joinPoint.proceed();
  }

  private void validateAnnotation(RolesAllowed rolesAllowed) {
    if (rolesAllowed == null || rolesAllowed.value().length == 0) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorMessage.MISSING_ANNOTATION.val());
    }
  }

  private Set<String> extractUserRolesFromHeader() {
    HttpServletRequest request = getCurrentHttpRequest();
    if (request == null) {
      throw new ResponseException(
          HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorMessage.REQUEST_CONTEXT_NOT_FOUND.val());
    }

    String userHeader = request.getHeader(SystemConstant.USER_HEADER);
    if (userHeader == null || userHeader.trim().isEmpty()) {
      throw new ResponseException(
          HttpStatus.UNAUTHORIZED, CommonErrorMessage.UNAUTHORIZED_MISSING_HEADER.val());
    }

    try {
      Map<String, String> userData = JsonParserUtils.toStringMap(userHeader);
      String rolesStr = userData.get(SystemConstant.ROLES_FIELD);

      if (rolesStr == null || rolesStr.trim().isEmpty()) {
        throw new ResponseException(HttpStatus.FORBIDDEN, CommonErrorMessage.FORBIDDEN.val());
      }

      return Arrays.stream(rolesStr.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toSet());

    } catch (Exception e) {
      throw new ResponseException(
          HttpStatus.BAD_REQUEST, CommonErrorMessage.INVALID_HEADER_FORMAT.val());
    }
  }

  private HttpServletRequest getCurrentHttpRequest() {
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return requestAttributes != null ? requestAttributes.getRequest() : null;
  }
}
