package com.manager.account.interfaces.rest.controllers;

import com.manager.account.interfaces.rest.dto.BaseResponseDTO;
import com.manager.account.interfaces.rest.dto.PageMeta;
import com.manager.account.interfaces.rest.dto.OrderDTOs;
import com.manager.account.domain.models.entities.Order;
import com.manager.account.domain.models.entities.OrderItem;
import com.manager.account.domain.models.enums.OrderStatus;
import com.manager.account.domain.models.entities.Table;
import com.manager.account.infrastructure.persistence.jpa.OrderRepository;
import com.manager.account.infrastructure.persistence.jpa.TableRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;

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

    private static double itemTotal(OrderItem i) {
        try {
            return i.getPrice() * i.getQuantity();
        } catch (NullPointerException e) {
            return 0d;
        }
    }

    private static double recomputeTotal(List<OrderItem> items) {
        return items.stream().mapToDouble(OrderController::itemTotal).sum();
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

    @GetMapping("/revenue-by-week")
    public BaseResponseDTO getRevenueByWeek(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(required = false) String time) {
        String resolvedServer = resolveServer(request, server);
        if (resolvedServer == null)
            return new BaseResponseDTO("ERROR", "Missing 'server'");

        Map<String, LocalDateTime> range = resolveTimeRange(time, null, null);
        List<Order> orders = orderRepository.findByServerAndStatusAndCreatedAtBetween(
                resolvedServer, OrderStatus.COMPLETED, range.get("from"), range.get("to"));

        // Tính doanh thu theo tuần trong tháng
        Map<Integer, Double> weekTotals = new LinkedHashMap<>();
        for (Order o : orders) {
            int week = o.getCreatedAt().getDayOfMonth() / 7 + 1;
            weekTotals.merge(week, o.getTotalAmount(), (v1, v2) -> v1 + v2);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : weekTotals.entrySet()) {
            result.add(Map.of("week", e.getKey(), "total", e.getValue()));
        }
        return new BaseResponseDTO("OK", "Success", result);
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
        List<Order> orders = orderRepository.findByServerAndCreatedAtBetween(
                resolvedServer, range.get("from"), range.get("to"));

        // Đếm số lần xuất hiện của từng món ăn
        Map<String, Long> countMap = new LinkedHashMap<>();
        Map<String, String> nameMap = new LinkedHashMap<>();
        for (Order o : orders) {
            for (OrderItem item : o.getItems()) {
                String id = item.getFoodId() != null ? item.getFoodId() : item.getFoodName();
                if (id == null)
                    continue;
                countMap.merge(id, (long) item.getQuantity(), (v1, v2) -> v1 + v2);
                nameMap.putIfAbsent(id, item.getFoodName() != null ? item.getFoodName() : id);
            }
        }

        if (countMap.isEmpty())
            return new BaseResponseDTO("OK", "No data", null);

        String topId = countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        Map<String, Object> data = Map.of(
                "foodId", topId != null ? topId : "",
                "foodName", topId != null ? nameMap.getOrDefault(topId, "") : "",
                "count", topId != null ? countMap.get(topId) : 0L);
        return new BaseResponseDTO("OK", "Success", data);
    }

    @Transactional
    @PostMapping("/create")
    public BaseResponseDTO createOrder(HttpServletRequest request,
            @Valid @RequestBody OrderDTOs.OrderRequest orderRequest) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        Table table = tableRepository.findByIdAndServer(orderRequest.getTableId(), server).orElse(null);
        if (table == null)
            return new BaseResponseDTO("ERROR", "Table not found");

        Order order = new Order();
        order.setId(java.util.UUID.randomUUID().toString());
        order.setTableId(orderRequest.getTableId());
        order.setServer(server);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(claims.getSubject());
        order.setStatus(OrderStatus.ORDERING);
        order.setTotalAmount(0);
        orderRepository.save(order);

        table.setCurrentOrderId(order.getId());
        tableRepository.save(table);
        return new BaseResponseDTO("OK", "Order created", Map.of("orderId", order.getId()));
    }

    @Transactional
    @PutMapping("/{id}/add-item")
    public BaseResponseDTO addItemToOrder(HttpServletRequest request, @PathVariable String id,
            @RequestBody OrderDTOs.AddOrderItemRequest newItem) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        Order order = orderRepository.findByIdAndServer(id, server).orElse(null);
        if (order == null)
            return new BaseResponseDTO("ERROR", "Order not found");

        OrderItem item = new OrderItem();
        item.setId(java.util.UUID.randomUUID().toString());
        item.setOrderItemId(java.util.UUID.randomUUID().toString());
        item.setFoodId(newItem.getFoodId());
        item.setFoodName(newItem.getFoodName());
        item.setPrice(newItem.getPrice());
        item.setQuantity(newItem.getQuantity());
        item.setOrder(order);
        order.getItems().add(item);
        order.setTotalAmount(recomputeTotal(order.getItems()));
        orderRepository.save(order);
        return new BaseResponseDTO("OK", "Item added");
    }

    @Transactional
    @PutMapping("/{id}/checkout")
    public BaseResponseDTO checkoutOrder(HttpServletRequest request, @PathVariable String id,
            @RequestBody OrderDTOs.CheckoutRequest body) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        Order order = orderRepository.findByIdAndServer(id, server).orElse(null);
        if (order == null)
            return new BaseResponseDTO("ERROR", "Order not found");

        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalAmount(recomputeTotal(order.getItems()));
        orderRepository.save(order);

        Table table = tableRepository.findByIdAndServer(order.getTableId(), server).orElse(null);
        if (table != null && id.equals(table.getCurrentOrderId())) {
            table.setCurrentOrderId(null);
            tableRepository.save(table);
        }
        return new BaseResponseDTO("OK", "Checked out");
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

    @Transactional
    @PutMapping("/{id}/waiter")
    public BaseResponseDTO updateWaiter(HttpServletRequest request, @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null)
            return new BaseResponseDTO("ERROR", "Unauthorized");
        String server = claims.get("server", String.class);
        String waiterId = body.get("waiterId");

        Optional<Order> opt = orderRepository.findByIdAndServer(id, server);
        if (opt.isEmpty())
            return new BaseResponseDTO("ERROR", "Order not found");

        Order order = opt.get();
        order.setWaiterId(waiterId);
        orderRepository.save(order);
        return new BaseResponseDTO("OK", "Waiter updated");
    }
}
