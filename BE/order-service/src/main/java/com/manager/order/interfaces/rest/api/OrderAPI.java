package com.manager.order.interfaces.rest.api;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.common.interfaces.rest.dto.PageMeta;
import com.manager.order.interfaces.rest.dto.OrderDTOs;
import com.manager.order.domain.models.entities.Order;
import com.manager.order.infrastructure.persistence.jpa.OrderRepository;
import com.manager.order.application.services.OrderService;
import com.manager.common.domain.models.enums.OrderItemStatus;
import com.manager.order.interfaces.rest.dto.response.OrderItemResponseDTO;
import com.manager.order.interfaces.rest.dto.response.OrderResponseDTO;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderAPI {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    private String resolveServer(HttpServletRequest request, String serverParam) {
        if (serverParam != null && !serverParam.isBlank())
            return serverParam;
        Object claimsObj = request.getAttribute("claims");
        if (claimsObj instanceof Claims) {
            Claims claims = (Claims) claimsObj;
            String fromToken = claims.get("server", String.class);
            if (fromToken != null && !fromToken.isBlank())
                return fromToken;
        }
        return null;
    }

    private Map<String, LocalDateTime> resolveTimeRange(String time, LocalDate start, LocalDate end) {
        LocalDateTime from, to;
        if (start != null && end != null) {
            from = start.atStartOfDay();
            to = end.atTime(23, 59, 59);
        } else if (time != null && !time.isBlank()) {
            YearMonth ym = YearMonth.parse(time, DateTimeFormatter.ofPattern("MM-yyyy"));
            from = ym.atDay(1).atStartOfDay();
            to = ym.atEndOfMonth().atTime(23, 59, 59);
        } else {
            YearMonth ym = YearMonth.now();
            from = ym.atDay(1).atStartOfDay();
            to = ym.atEndOfMonth().atTime(23, 59, 59);
        }
        return Map.of("from", from, "to", to);
    }

    @GetMapping("/list")
    public BaseResponseDTO getOrdersByServerAndTime(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(required = false) String time) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null) {
            return new BaseResponseDTO("ERROR", "Missing 'server' (param or token)");
        }
        Map<String, LocalDateTime> range = resolveTimeRange(time, null, null);
        List<Order> orders = orderRepository.findByServerAndCreatedAtBetween(resolvedServer, range.get("from"),
                range.get("to"));
        return new BaseResponseDTO("OK", "Success", orders);
    }

    @GetMapping("/items/pending")
    public BaseResponseDTO getPendingItems(HttpServletRequest request,
                                           @RequestParam(required = false) String server) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null) {
            return new BaseResponseDTO("ERROR", "Missing 'server'");
        }
        List<OrderItemResponseDTO> items = orderService.getPendingItems(resolvedServer);
        return new BaseResponseDTO("OK", "Success", items);
    }

    @GetMapping("/completed")
    public BaseResponseDTO getCompletedOrders(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam String from,
            @RequestParam String to) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null) {
            return new BaseResponseDTO("ERROR", "Missing 'server'");
        }
        try {
            LocalDateTime fromDt = LocalDateTime.parse(from);
            LocalDateTime toDt = LocalDateTime.parse(to);
            List<OrderResponseDTO> orders = orderService.getCompletedOrders(resolvedServer, fromDt, toDt);
            return new BaseResponseDTO("OK", "Success", orders);
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", "Invalid date format or range: " + e.getMessage());
        }
    }

    @PutMapping("/items/{orderItemId}/status")
    public BaseResponseDTO updateItemStatus(HttpServletRequest request,
                                            @PathVariable String orderItemId,
                                            @RequestBody Map<String, String> body) {
        String server = resolveServer(request, null);
        if (server == null) {
            return new BaseResponseDTO("ERROR", "Missing server in token");
        }
        String statusStr = body.get("status");
        if (statusStr == null) {
            return new BaseResponseDTO("ERROR", "Missing status");
        }
        try {
            OrderItemStatus status = OrderItemStatus.valueOf(statusStr);
            orderService.updateItemStatus(orderItemId, server, status);
            return new BaseResponseDTO("OK", "Status updated successfully");
        } catch (IllegalArgumentException e) {
            return new BaseResponseDTO("ERROR", "Invalid status: " + statusStr);
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }

    @GetMapping("/revenue-by-week")
    public BaseResponseDTO getRevenueByWeek(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(required = false) String time) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null)
            return new BaseResponseDTO("ERROR", "Missing 'server'");

        Map<String, LocalDateTime> range = resolveTimeRange(time, null, null);
        Object data = orderService.getRevenueByWeek(resolvedServer, range.get("from"), range.get("to"));
        return new BaseResponseDTO("OK", "Success", data);
    }

    @GetMapping("/most-favorite-food")
    public BaseResponseDTO getMostFavoriteFood(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(required = false) String time) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null)
            return new BaseResponseDTO("ERROR", "Missing 'server'");

        Map<String, LocalDateTime> range = resolveTimeRange(time, null, null);
        Object data = orderService.getMostFavoriteFood(resolvedServer, range.get("from"), range.get("to"));
        return new BaseResponseDTO("OK", "Success", data);
    }

    @PostMapping("/create")
    public BaseResponseDTO createOrder(HttpServletRequest request,
            @Valid @RequestBody OrderDTOs.OrderRequest orderRequest) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        
        try {
            String orderId = orderService.createOrder(claims.getSubject(), server, orderRequest.getTableId());
            return new BaseResponseDTO("OK", "Order created", Map.of("orderId", orderId));
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }

    @PutMapping("/{id}/add-item")
    public BaseResponseDTO addItemToOrder(HttpServletRequest request, @PathVariable String id,
            @RequestBody OrderDTOs.AddOrderItemRequest newItem) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        
        try {
            orderService.addItemToOrder(id, server, newItem);
            return new BaseResponseDTO("OK", "Item added");
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }

    @PutMapping("/{id}/checkout")
    public BaseResponseDTO checkoutOrder(HttpServletRequest request, @PathVariable String id,
            @RequestBody OrderDTOs.CheckoutRequest body) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        
        try {
            Object receipt = orderService.checkout(id, server, body);
            return new BaseResponseDTO("OK", "Checked out", receipt);
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }

    @GetMapping
    public BaseResponseDTO listOrders(HttpServletRequest request, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> pageData = orderRepository.findAllByServer(server, pageable);
        PageMeta meta = new PageMeta(
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements());
        return new BaseResponseDTO("OK", "Success", pageData.getContent(), meta);
    }

    @PutMapping("/{id}/waiter")
    public BaseResponseDTO updateWaiter(HttpServletRequest request, @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        String waiterId = body.get("waiterId");

        try {
            orderService.updateWaiter(id, server, waiterId);
            return new BaseResponseDTO("OK", "Waiter updated");
        } catch (Exception e) {
            return new BaseResponseDTO("ERROR", e.getMessage());
        }
    }
}
