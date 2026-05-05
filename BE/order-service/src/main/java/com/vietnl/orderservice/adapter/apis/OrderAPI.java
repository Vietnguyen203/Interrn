package com.vietnl.orderservice.adapter.apis;

import com.vietnl.orderservice.application.requests.CreateOrderRequest;
import com.vietnl.orderservice.application.requests.OrderItemRequest;
import com.vietnl.orderservice.application.requests.UpdateOrderItemRequest;
import com.vietnl.orderservice.application.responses.ApiResponse;
import com.vietnl.orderservice.application.responses.OrderResponse;
import com.vietnl.orderservice.application.usecases.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderAPI {

    private final OrderService orderService;

    // POST /orders - Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "unknown";
        OrderResponse response = orderService.createOrder(request, username);
        return ResponseEntity.ok(ApiResponse.ok("Tạo đơn hàng thành công", response));
    }

    // GET /orders - Lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status) {
        List<OrderResponse> orders = (status != null && !status.isBlank())
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    // GET /orders/{id} - Lấy đơn theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    // POST /orders/{id}/items - Thêm món vào đơn
    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Thêm món thành công", orderService.addItemToOrder(id, request)));
    }

    // PUT /orders/{id}/items/{itemId} - Cập nhật số lượng món
    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateOrderItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật món thành công", orderService.updateOrderItem(id, itemId, request)));
    }

    // DELETE /orders/{id}/items/{itemId} - Xóa món khỏi đơn
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderResponse>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.ok("Xóa món thành công", orderService.removeItemFromOrder(id, itemId)));
    }

    // PATCH /orders/{id}/status - Cập nhật trạng thái đơn
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", orderService.updateOrderStatus(id, status)));
    }

    // DELETE /orders/{id} - Hủy đơn hàng
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn thành công", null));
    }

    // PATCH /orders/items/{itemId}/kitchen-status - Bếp cập nhật trạng thái chế biến
    @PatchMapping("/items/{itemId}/kitchen-status")
    public ResponseEntity<ApiResponse<Void>> updateKitchenStatus(
            @PathVariable UUID itemId,
            @RequestParam String status) {
        orderService.updateKitchenStatus(itemId, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái bếp thành công", null));
    }
}
