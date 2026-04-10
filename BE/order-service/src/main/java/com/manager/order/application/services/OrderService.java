package com.manager.order.application.services;
 
import com.manager.common.domain.models.enums.OrderItemStatus;
import com.manager.common.domain.models.enums.OrderStatus;
import com.manager.order.domain.models.entities.Order;
import com.manager.order.domain.models.entities.OrderItem;
import com.manager.order.domain.models.entities.Table;
import com.manager.order.infrastructure.persistence.jpa.OrderItemRepository;
import com.manager.order.infrastructure.persistence.jpa.OrderRepository;
import com.manager.order.infrastructure.persistence.jpa.TableRepository;
import com.manager.order.interfaces.rest.dto.OrderDTOs;
import com.manager.order.interfaces.rest.dto.response.OrderItemResponseDTO;
import com.manager.order.interfaces.rest.dto.response.OrderResponseDTO;
import com.manager.order.interfaces.rest.dto.response.ReceiptDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TableRepository tableRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    private OrderItemResponseDTO mapToDTO(OrderItem item) {
        return OrderItemResponseDTO.builder()
                .orderItemId(item.getOrderItemId())
                .orderId(item.getOrder().getId())
                .foodId(item.getFoodId())
                .foodName(item.getFoodName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .note(item.getNote())
                .status(item.getStatus())
                .server(item.getOrder().getServer())
                .build();
    }

    public List<OrderItemResponseDTO> getPendingItems(String server) {
        List<OrderItemStatus> statuses = Arrays.asList(OrderItemStatus.PENDING, OrderItemStatus.PREPARING);
        return orderItemRepository.findByOrderServerAndStatusIn(server, statuses)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateItemStatus(String orderItemId, String server, OrderItemStatus status) {
        OrderItem item = orderItemRepository.findByOrderItemIdAndOrderServer(orderItemId, server)
                .orElseThrow(() -> new RuntimeException("OrderItem not found or server mismatch"));
        item.setStatus(status);
        orderItemRepository.save(item);
    }

    private OrderResponseDTO mapOrderToDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .server(order.getServer())
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .waiterId(order.getWaiterId())
                .items(order.getItems().stream().map(this::mapToDTO).collect(Collectors.toList()))
                .build();
    }

    public List<OrderResponseDTO> getCompletedOrders(String server, LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByServerAndStatusAndCreatedAtBetween(server, OrderStatus.COMPLETED, from, to)
                .stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    /* NEW: đảm bảo mọi dòng món đều có orderItemId (id subdocument) */
    public Order ensureLineIds(Order order) {
        if (order == null || order.getItems() == null)
            return order;
        boolean mutated = false;
        for (OrderItem it : order.getItems()) {
            String lineId = it.getOrderItemId(); // ánh xạ sang id nội bộ
            if (lineId == null || lineId.isBlank()) {
                it.setOrderItemId(UUID.randomUUID().toString());
                mutated = true;
            }
        }
        if (mutated)
            orderRepository.save(order);
        return order;
    }

    private static double itemTotal(OrderItem i) {
        try {
            return i.getPrice() * i.getQuantity();
        } catch (NullPointerException e) {
            return 0d;
        }
    }

    private static double recomputeTotal(List<OrderItem> items) {
        return items.stream().mapToDouble(OrderService::itemTotal).sum();
    }

    @Transactional
    public ReceiptDTO checkout(String orderId, String server, OrderDTOs.CheckoutRequest req) {
        Order order = orderRepository.findByIdAndServer(orderId, server)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.ORDERING) {
            throw new RuntimeException("Order is not in ORDERING status");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order has no items");
        }

        // đảm bảo dòng có id trước khi chốt
        ensureLineIds(order);

        double subtotal = recomputeTotal(order.getItems());
        double discount = req.getDiscount() == null ? 0d : Math.max(0d, req.getDiscount());
        double total = Math.max(0d, subtotal - discount);

        Double amountReceived = req.getAmountReceived();
        if (amountReceived == null || amountReceived < total) {
            throw new RuntimeException("Insufficient amount");
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        Table table = tableRepository.findByIdAndServer(order.getTableId(), server).orElse(null);
        if (table != null && orderId.equals(table.getCurrentOrderId())) {
            table.setCurrentOrderId(null);
            tableRepository.save(table);
        }

        ReceiptDTO r = new ReceiptDTO();
        r.setOrderId(order.getId());
        r.setTableId(order.getTableId());
        r.setSubtotal(subtotal);
        r.setDiscount(discount);
        r.setTotal(total);
        r.setPaymentMethod(req.getPaymentMethod());
        r.setAmountReceived(amountReceived);
        r.setChange(amountReceived - total);
        r.setPaidAtEpochMs(System.currentTimeMillis());
        return r;
    }

    @Transactional
    public String createOrder(String waiterId, String server, String tableId) {
        Table table = tableRepository.findByIdAndServer(tableId, server)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setTableId(tableId);
        order.setServer(server);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(waiterId);
        order.setStatus(OrderStatus.ORDERING);
        order.setTotalAmount(0);
        orderRepository.save(order);

        table.setCurrentOrderId(order.getId());
        tableRepository.save(table);
        return order.getId();
    }

    @Transactional
    public void addItemToOrder(String orderId, String server, OrderDTOs.AddOrderItemRequest newItem) {
        Order order = orderRepository.findByIdAndServer(orderId, server)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderItem item = new OrderItem();
        item.setId(UUID.randomUUID().toString());
        item.setOrderItemId(UUID.randomUUID().toString());
        item.setFoodId(newItem.getFoodId());
        item.setFoodName(newItem.getFoodName());
        item.setPrice(newItem.getPrice());
        item.setQuantity(newItem.getQuantity());
        item.setOrder(order);
        order.getItems().add(item);
        order.setTotalAmount(recomputeTotal(order.getItems()));
        orderRepository.save(order);
    }

    @Transactional
    public void updateWaiter(String orderId, String server, String waiterId) {
        Order order = orderRepository.findByIdAndServer(orderId, server)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setWaiterId(waiterId);
        orderRepository.save(order);
    }

    public List<Map<String, Object>> getRevenueByWeek(String server, LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findByServerAndStatusAndCreatedAtBetween(
                server, OrderStatus.COMPLETED, from, to);

        Map<Integer, Double> weekTotals = new java.util.LinkedHashMap<>();
        for (Order o : orders) {
            int week = o.getCreatedAt().getDayOfMonth() / 7 + 1;
            weekTotals.merge(week, o.getTotalAmount(), (v1, v2) -> v1 + v2);
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<Integer, Double> e : weekTotals.entrySet()) {
            result.add(Map.of("week", e.getKey(), "total", e.getValue()));
        }
        return result;
    }

    public Map<String, Object> getMostFavoriteFood(String server, LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findByServerAndCreatedAtBetween(server, from, to);

        Map<String, Long> countMap = new java.util.LinkedHashMap<>();
        Map<String, String> nameMap = new java.util.LinkedHashMap<>();
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
            return null;

        String topId = countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey).orElse(null);

        return Map.of(
                "foodId", topId != null ? topId : "",
                "foodName", topId != null ? nameMap.getOrDefault(topId, "") : "",
                "count", topId != null ? countMap.get(topId) : 0L);
    }
}
