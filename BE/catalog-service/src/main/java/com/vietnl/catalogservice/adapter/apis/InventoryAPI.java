package com.vietnl.catalogservice.adapter.apis;

import com.vietnl.catalogservice.application.requests.IngredientRequest;
import com.vietnl.catalogservice.application.requests.RecipeItemRequest;
import com.vietnl.catalogservice.application.requests.StockTransactionRequest;
import com.vietnl.catalogservice.application.requests.DeductStockRequest;
import com.vietnl.catalogservice.domain.entities.Ingredient;
import com.vietnl.catalogservice.domain.entities.Recipe;
import com.vietnl.catalogservice.domain.entities.InventoryTransaction;
import com.vietnl.catalogservice.application.usecases.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/catalog-service/inventory")
@RequiredArgsConstructor
public class InventoryAPI {

    private final InventoryService inventoryService;

    // --- Ingredients CRUD ---

    @GetMapping("/ingredients")
    public ResponseEntity<?> getAllIngredients() {
        List<Ingredient> list = inventoryService.getAllIngredients();
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable UUID id) {
        Ingredient ing = inventoryService.getIngredientById(id);
        return ResponseEntity.ok(Map.of("code", "200", "data", ing));
    }

    @PostMapping("/ingredients")
    public ResponseEntity<?> createIngredient(@RequestBody IngredientRequest request) {
        Ingredient created = inventoryService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("code", "201", "message", "Thêm nguyên vật liệu thành công", "data", created));
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<?> updateIngredient(@PathVariable UUID id, @RequestBody IngredientRequest request) {
        Ingredient updated = inventoryService.updateIngredient(id, request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Cập nhật nguyên vật liệu thành công", "data", updated));
    }

    @DeleteMapping("/ingredients/{id}")
    public ResponseEntity<?> deleteIngredient(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        inventoryService.deleteIngredient(id, reason);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Xóa nguyên vật liệu thành công"));
    }

    // --- Stock Transactions ---

    @PostMapping("/transactions/import")
    public ResponseEntity<?> importStock(@RequestBody StockTransactionRequest request) {
        InventoryTransaction tx = inventoryService.importStock(request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Nhập kho thành công", "data", tx));
    }

    @PostMapping("/transactions/export")
    public ResponseEntity<?> exportStock(@RequestBody StockTransactionRequest request) {
        InventoryTransaction tx = inventoryService.exportStock(request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Xuất hủy kho thành công", "data", tx));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "ALL") String shift,
            @RequestParam(defaultValue = "ALL") String period) {
        List<InventoryTransaction> list = "ALL".equalsIgnoreCase(period) && date == null && "ALL".equalsIgnoreCase(shift)
                ? inventoryService.getAllTransactions()
                : inventoryService.getTransactions(date, period, shift);
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    @GetMapping("/transactions/summary")
    public ResponseEntity<?> getTransactionSummary(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "ALL") String shift,
            @RequestParam(defaultValue = "DAY") String period) {
        return ResponseEntity.ok(Map.of("code", "200", "data", inventoryService.getInventorySummary(date, period, shift)));
    }

    // --- Recipe Management ---

    @GetMapping("/recipes/{menuItemId}")
    public ResponseEntity<?> getRecipesForMenuItem(@PathVariable UUID menuItemId) {
        List<Recipe> list = inventoryService.getRecipesForMenuItem(menuItemId);
        return ResponseEntity.ok(Map.of("code", "200", "data", list));
    }

    @PostMapping("/recipes/{menuItemId}")
    public ResponseEntity<?> updateRecipeForMenuItem(@PathVariable UUID menuItemId, @RequestBody List<RecipeItemRequest> request) {
        List<Recipe> list = inventoryService.updateRecipeForMenuItem(menuItemId, request);
        return ResponseEntity.ok(Map.of("code", "200", "message", "Cập nhật công thức định lượng thành công", "data", list));
    }

    // --- Stock Deduction ---

    @PostMapping("/deduct")
    public ResponseEntity<?> deductStockForOrder(@RequestBody DeductStockRequest request) {
        try {
            inventoryService.deductStockForOrder(request);
            return ResponseEntity.ok(Map.of("code", "200", "message", "Khấu trừ kho hàng thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", "400", "message", e.getMessage()));
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundStockForOrder(@RequestBody DeductStockRequest request) {
        try {
            inventoryService.refundStockForOrder(request);
            return ResponseEntity.ok(Map.of("code", "200", "message", "Hoàn trả kho hàng thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", "400", "message", e.getMessage()));
        }
    }
}
