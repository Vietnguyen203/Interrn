package com.vietnl.sharedlibrary.core.context;

import com.eps.shared.core.json.JsonParserUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Component
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HeaderContext implements Serializable {

  private UUID userId;
  private UUID personId;
  private UUID orgId;
  private String name;
  private String userCode;

  @JsonProperty("isAdmin")
  private boolean isAdmin;

  private String deviceId;
  private UUID ssoId;
  private String roles;
  private String email;
  private String applicationCode;
  private String clientId;
  private String traceId;
  private String userType;

  private boolean isAuthenticated = false;

  private Map<String, Object> extraData = new HashMap<>();

  public HeaderContext(String xUserHeader) {

    Map<String, Object> extraData = JsonParserUtils.toObjectMap(xUserHeader);
    traceId = UUID.randomUUID().toString();

    if (xUserHeader != null) {
      if (extraData.get("userId") != null) {
        userId = UUID.fromString((String) extraData.get("userId"));
      }
      if (extraData.get("ssoId") != null) {
        ssoId = UUID.fromString((String) extraData.get("ssoId"));
      }
      if (extraData.get("personId") != null) {
        personId = UUID.fromString((String) extraData.get("personId"));
      }
      if (extraData.get("orgId") != null) {
        orgId = UUID.fromString((String) extraData.get("orgId"));
      }
      name = (String) extraData.get("name");
      email = (String) extraData.get("email");
      userCode = (String) extraData.get("userCode");
      applicationCode = (String) extraData.get("applicationCode");
      deviceId = (String) extraData.get("deviceId");
      roles = (String) extraData.get("roles");

      isAdmin = (Boolean) extraData.get("isAdmin");
      if (roles != null) {
        isAdmin =
            Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(role -> role.equals("admin"));
      }
      userType = (String) extraData.get("type");

      isAuthenticated = true;
    }
  }
}
