package com.vietnl.catalogservice.adapter.apis;

import com.vietnl.catalogservice.application.requests.CategoryRequest;
import com.vietnl.catalogservice.domain.entities.Category;
import com.vietnl.catalogservice.application.usecases.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalog-service/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class CategoryAPI {

    private final CategoryService categoryService;

    // GET all categories
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Category> list = categoryService.getAll();
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    // GET category by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("code", "200", "data", categoryService.getById(id)));
    }

    // POST create category
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategoryRequest request) {
        Category created = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", "201", "message", "Tạo danh mục thành công", "data", created));
    }

    // PUT update category
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody CategoryRequest request) {
        Category updated = categoryService.update(id, request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Cập nhật danh mục thành công", "data", updated));
    }

    // DELETE category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Xóa danh mục thành công"));
    }
}
