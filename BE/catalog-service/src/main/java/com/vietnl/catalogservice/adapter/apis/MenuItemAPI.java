package com.vietnl.catalogservice.adapter.apis;

import com.vietnl.catalogservice.application.requests.MenuItemRequest;
import com.vietnl.catalogservice.domain.entities.MenuItem;
import com.vietnl.catalogservice.application.usecases.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalog-service/items")
@RequiredArgsConstructor
public class MenuItemAPI {

    private final MenuItemService menuItemService;

    // GET all items
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<MenuItem> list = menuItemService.getAll();
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    // GET items by category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategoryId(@PathVariable String categoryId) {
        List<MenuItem> list = menuItemService.getByCategoryId(categoryId);
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    // GET item by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", "200", "data", menuItemService.getById(id)));
    }

    // POST create item
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MenuItemRequest request) {
        MenuItem created = menuItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", "201", "message", "Thêm món ăn thành công", "data", created));
    }

    // PUT update item
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody MenuItemRequest request) {
        MenuItem updated = menuItemService.update(id, request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Cập nhật món ăn thành công", "data", updated));
    }

    // DELETE item
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        menuItemService.delete(id);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Xóa món ăn thành công"));
    }

    // POST propose new item (Kitchen)
    @PostMapping("/propose")
    public ResponseEntity<?> propose(@RequestBody MenuItemRequest request) {
        MenuItem proposed = menuItemService.propose(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", "201", "message", "Gửi đề xuất món ăn thành công", "data", proposed));
    }

    // PUT approve item (Admin)
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        MenuItem approved = menuItemService.approve(id);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Duyệt món ăn thành công", "data", approved));
    }

    // PUT reject item (Admin)
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        MenuItem rejected = menuItemService.reject(id);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Từ chối món ăn thành công", "data", rejected));
    }
}
