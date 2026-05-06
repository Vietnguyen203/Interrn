package com.vietnl.tableservice.adapter.apis;

import com.vietnl.tableservice.application.dto.TableRequest;
import com.vietnl.tableservice.application.usecases.TableService;
import com.vietnl.tableservice.domain.entities.RestaurantTable;
import com.vietnl.tableservice.domain.enums.TableStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class TableAPI {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) TableStatus status) {
        List<RestaurantTable> tables = (status != null)
                ? tableService.getByStatus(status)
                : tableService.getAll();
        return ResponseEntity.ok(Map.of("code", 200, "data", tables));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("code", 200, "data", tableService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TableRequest request) {
        RestaurantTable table = tableService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", 201, "message", "Tạo bàn thành công", "data", table));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody TableRequest request) {
        RestaurantTable table = tableService.update(id, request);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Cập nhật thành công", "data", table));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "Thiếu trường 'status' trong body request"));
        }
        try {
            TableStatus status = TableStatus.valueOf(statusStr.toUpperCase());
            RestaurantTable table = tableService.updateStatus(id, status);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Cập nhật trạng thái thành công", "data", table));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "Trạng thái '" + statusStr + "' không hợp lệ"));
        }
    }

    @PatchMapping("/{id}/assign-order")
    public ResponseEntity<?> assignOrder(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String orderId = body.get("orderId");
        RestaurantTable table = tableService.assignOrder(id, orderId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Đã gán đơn hàng vào bàn", "data", table));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<?> release(@PathVariable UUID id) {
        RestaurantTable table = tableService.updateStatus(id, TableStatus.CLEANING);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Đã giải phóng bàn, chờ dọn dẹp", "data", table));
    }

    @PostMapping("/{id}/ready")
    public ResponseEntity<?> ready(@PathVariable UUID id) {
        RestaurantTable table = tableService.updateStatus(id, TableStatus.AVAILABLE);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Bàn đã sẵn sàng đón khách", "data", table));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        tableService.delete(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "Xóa bàn thành công"));
    }
}