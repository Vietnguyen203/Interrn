package com.manager.kitchen.interfaces.rest.controllers;

import com.manager.order.domain.models.entities.OrderItem;
import com.manager.order.domain.models.enums.OrderItemStatus;
import com.manager.order.infrastructure.persistence.jpa.OrderItemRepository;
import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kitchen")
@RequiredArgsConstructor
public class KitchenController {

    private final OrderItemRepository orderItemRepository;

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

        List<OrderItemStatus> statuses = Arrays.asList(OrderItemStatus.PENDING, OrderItemStatus.PREPARING);
        List<OrderItem> items = orderItemRepository.findByOrderServerAndStatusIn(server, statuses);

        return new BaseResponseDTO("OK", "Success", items);
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

        OrderItemStatus newStatus;
        try {
            newStatus = OrderItemStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return new BaseResponseDTO("ERROR", "Invalid status: " + statusStr);
        }

        OrderItem item = orderItemRepository.findByOrderItemIdAndOrderServer(orderItemId, server)
                .orElse(null);

        if (item == null) {
            return new BaseResponseDTO("ERROR", "Item not found");
        }

        // Workflow validation (optional but good)
        // PENDING -> PREPARING -> READY
        item.setStatus(newStatus);
        orderItemRepository.save(item);

        return new BaseResponseDTO("OK", "Status updated successfully. Item is now " + newStatus);
    }
}
