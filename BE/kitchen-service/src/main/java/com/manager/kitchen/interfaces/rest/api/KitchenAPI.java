package com.manager.kitchen.interfaces.rest.api;

import com.manager.kitchen.infrastructure.clients.OrderServiceClient;
import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/kitchen")
@RequiredArgsConstructor
public class KitchenAPI {

    private final OrderServiceClient orderServiceClient;

    private String resolveServer(HttpServletRequest request) {
        Object claimsObj = request.getAttribute("claims");
        if (claimsObj instanceof Claims) {
            Claims claims = (Claims) claimsObj;
            return claims.get("server", String.class);
        }
        return null;
    }

    @GetMapping("/items")
    public BaseResponseDTO getPendingItems(HttpServletRequest request) {
        String server = resolveServer(request);
        if (server == null) {
            return new BaseResponseDTO("ERROR", "Missing server in token");
        }

        // Only KITCHEN or ADMIN can access kitchen items
        Object claimsObj = request.getAttribute("claims");
        if (claimsObj instanceof Claims) {
            Claims claims = (Claims) claimsObj;
            String role = claims.get("role", String.class);
            if (!"KITCHEN".equals(role) && !"ADMIN".equals(role)) {
                return new BaseResponseDTO("ERROR", "Access denied: Kitchen/Admin role required");
            }
        }

        return orderServiceClient.getPendingItems(server);
    }

    @PutMapping("/items/{orderItemId}/status")
    public BaseResponseDTO updateItemStatus(
            HttpServletRequest request,
            @PathVariable String orderItemId,
            @RequestBody Map<String, String> body) {

        String server = resolveServer(request);
        if (server == null) {
            return new BaseResponseDTO("ERROR", "Missing server in token");
        }

        // Role check
        Object claimsObj = request.getAttribute("claims");
        if (claimsObj instanceof Claims) {
            Claims claims = (Claims) claimsObj;
            String role = claims.get("role", String.class);
            if (!"KITCHEN".equals(role) && !"ADMIN".equals(role)) {
                return new BaseResponseDTO("ERROR", "Access denied: Kitchen/Admin role required");
            }
        }

        String statusStr = body.get("status");
        if (statusStr == null) {
            return new BaseResponseDTO("ERROR", "Missing status");
        }

        // Trực tiếp chuyển tiếp yêu cầu sang order-service qua Feign Client
        return orderServiceClient.updateItemStatus(orderItemId, body);
    }
}
