package com.vietnl.orderservice.application.usecases;

import com.vietnl.orderservice.application.requests.CreateOrderRequest;
import com.vietnl.orderservice.application.requests.OrderItemRequest;
import com.vietnl.orderservice.application.requests.UpdateOrderItemRequest;
import com.vietnl.orderservice.application.responses.OrderResponse;
import com.vietnl.orderservice.domain.models.entities.Order;
import com.vietnl.orderservice.domain.models.entities.OrderItem;
import com.vietnl.orderservice.infrastructure.persistence.repositories.OrderItemRepository;
import com.vietnl.orderservice.infrastructure.persistence.repositories.OrderRepository;
import com.vietnl.orderservice.infrastructure.communication.TableFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    private final TableFeignClient tableFeignClient;

    // ===== TẠO ĐƠN HÀNG =====
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username, String token) {
        Order order = new Order();
        order.setTableId(request.getTableId());
        order.setTableNumber(request.getTableNumber());
        order.setNote(request.getNote());
        order.setCreatedBy(username);
        order.setStatus("PENDING");

        // Thêm các món (null-safe: mobile book bàn có thể không kèm items)
        if (request.getItems() != null) {
            for (OrderItemRequest itemReq : request.getItems()) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setMenuItemId(itemReq.getMenuItemId());
                item.setFoodName(itemReq.getFoodName());
                item.setUnitPrice(itemReq.getUnitPrice());
                item.setQuantity(itemReq.getQuantity());
                item.setNote(itemReq.getNote());
                item.setKitchenStatus("PENDING");
                order.getItems().add(item);
            }
        }

        order.recalculateTotal();
        Order saved = orderRepository.save(order);

        // Báo cho table-service để gán đơn vào bàn (đổi màu thành OCCUPIED)
        if (request.getTableId() != null && token != null) {
            try {
                String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", authToken);
                headers.set("X-Server", "server-1"); 
                
                System.out.println(">>> [SYNC]: Gọi table-service (Feign) gán đơn cho bàn " + request.getTableId());
                tableFeignClient.assignOrder(
                        UUID.fromString(request.getTableId()),
                        Map.of("orderId", saved.getId().toString()),
                        authToken,
                        "server-1"
                );
            } catch (Exception e) {
                System.err.println("Lỗi khi đồng bộ trạng thái bàn (Feign): " + e.getMessage());
            }
        }

        return OrderResponse.from(saved);
    }

    // ===== LẤY TẤT CẢ ĐƠN HÀNG =====
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // ===== LẤY ĐƠN THEO STATUS =====
    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // ===== LẤY ĐƠN THEO ID =====
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + id));
        return OrderResponse.from(order);
    }

    // ===== THÊM MÓN VÀO ĐƠN =====
    @Transactional
    public OrderResponse addItemToOrder(UUID orderId, OrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể thêm món vào đơn hàng đã kết thúc");
        }

        order.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst()
                .ifPresentOrElse(
                    existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                    () -> {
                        OrderItem item = new OrderItem();
                        item.setOrder(order);
                        item.setMenuItemId(request.getMenuItemId());
                        item.setFoodName(request.getFoodName());
                        item.setUnitPrice(request.getUnitPrice());
                        item.setQuantity(request.getQuantity());
                        item.setNote(request.getNote());
                        item.setKitchenStatus("PENDING");
                        order.getItems().add(item);
                    }
                );

        order.recalculateTotal();
        return OrderResponse.from(orderRepository.save(order));
    }

    // ===== CẬP NHẬT MÓN TRONG ĐƠN =====
    @Transactional
    public OrderResponse updateOrderItem(UUID orderId, UUID itemId, UpdateOrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món: " + itemId));

        item.setQuantity(request.getQuantity());
        if (request.getNote() != null) item.setNote(request.getNote());

        order.recalculateTotal();
        return OrderResponse.from(orderRepository.save(order));
    }

    // ===== XÓA MÓN KHỎI ĐƠN =====
    @Transactional
    public OrderResponse removeItemFromOrder(UUID orderId, UUID itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

        order.getItems().removeIf(i -> i.getId().equals(itemId));
        order.recalculateTotal();
        return OrderResponse.from(orderRepository.save(order));
    }

    // ===== CẬP NHẬT TRẠNG THÁI ĐƠN =====
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, String status, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);

        if (("COMPLETED".equals(status) || "CANCELLED".equals(status)) && token != null) {
            if (order.getTableId() != null) {
                long activeOrdersCount = orderRepository.countByTableIdAndStatusIn(order.getTableId(), List.of("PENDING", "CONFIRMED"));
                if (activeOrdersCount == 0) {
                    try {
                        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.set("Authorization", authToken);
                        headers.set("X-Server", "server-1");
                        
                        System.out.println(">>> [SYNC]: Gọi table-service (Feign) giải phóng bàn " + order.getTableId());
                        tableFeignClient.assignOrder(
                                UUID.fromString(order.getTableId()),
                                Map.of("orderId", ""),
                                authToken,
                                "server-1"
                        );
                    } catch (Exception e) {
                        System.err.println("Lỗi khi giải phóng bàn (Feign): " + e.getMessage());
                    }
                }
            }
        }

        return OrderResponse.from(saved);
    }

    public String getCreatorByTable(String tableId) {
        return orderRepository.findByTableIdAndStatusIn(tableId, List.of("PENDING", "CONFIRMED"))
                .stream()
                .findFirst()
                .map(Order::getCreatedBy)
                .orElse("N/A");
    }

    // ===== XÓA ĐƠN HÀNG =====
    @Transactional
    public void cancelOrder(UUID orderId, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));
        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành");
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        if (order.getTableId() != null && token != null) {
            long activeOrdersCount = orderRepository.countByTableIdAndStatusIn(order.getTableId(), List.of("PENDING", "CONFIRMED"));
            if (activeOrdersCount == 0) {
                try {
                    String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("Authorization", authToken);
                    headers.set("X-Server", "server-1");
                    
                    System.out.println(">>> [SYNC]: Gọi table-service (Feign) giải phóng bàn (hủy đơn) " + order.getTableId());
                    tableFeignClient.assignOrder(
                            UUID.fromString(order.getTableId()),
                            Map.of("orderId", ""),
                            authToken,
                            "server-1"
                    );
                } catch (Exception e) {
                    System.err.println("Lỗi khi giải phóng bàn (hủy đơn - Feign): " + e.getMessage());
                }
            }
        }
    }

    // ===== CẬP NHẬT TRẠNG THÁI BẾP =====
    @Transactional
    public void updateKitchenStatus(UUID itemId, String kitchenStatus) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món: " + itemId));
        item.setKitchenStatus(kitchenStatus);
        orderItemRepository.save(item);
    }


}
