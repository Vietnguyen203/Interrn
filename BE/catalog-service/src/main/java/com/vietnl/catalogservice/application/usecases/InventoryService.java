package com.vietnl.catalogservice.application.usecases;

import com.vietnl.catalogservice.application.requests.IngredientRequest;
import com.vietnl.catalogservice.application.requests.RecipeItemRequest;
import com.vietnl.catalogservice.application.requests.StockTransactionRequest;
import com.vietnl.catalogservice.application.requests.DeductStockRequest;
import com.vietnl.catalogservice.domain.entities.Ingredient;
import com.vietnl.catalogservice.domain.entities.Recipe;
import com.vietnl.catalogservice.domain.entities.InventoryTransaction;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.IngredientRepository;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.RecipeRepository;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("ALTER TABLE ingredients ADD is_active NUMBER(1,0) DEFAULT 1 NOT NULL");
            System.out.println("ADDED is_active COLUMN TO INGREDIENTS TABLE");
        } catch (Exception e) {
            System.out.println("is_active column might already exist: " + e.getMessage());
        }
    }

    // --- Ingredients CRUD ---

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .filter(Ingredient::isActive)
                .collect(java.util.stream.Collectors.toList());
    }

    public Ingredient getIngredientById(UUID id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nguyên vật liệu không tồn tại!"));
    }

    public Ingredient createIngredient(IngredientRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setUnit(request.getUnit());
        ingredient.setMinStock(request.getMinStock() != null ? request.getMinStock() : BigDecimal.ZERO);
        ingredient.setCurrentStock(BigDecimal.ZERO);
        return ingredientRepository.save(ingredient);
    }

    public Ingredient updateIngredient(UUID id, IngredientRequest request) {
        Ingredient ingredient = getIngredientById(id);
        if (request.getName() != null) ingredient.setName(request.getName());
        if (request.getUnit() != null) ingredient.setUnit(request.getUnit());
        if (request.getMinStock() != null) ingredient.setMinStock(request.getMinStock());
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public void deleteIngredient(UUID id, String reason) {
        Ingredient ingredient = ingredientRepository.findById(id).orElse(null);
        if (ingredient != null) {
            boolean hasStock = ingredient.getCurrentStock() != null && ingredient.getCurrentStock().compareTo(BigDecimal.ZERO) > 0;
            if (hasStock || (reason != null && !reason.isBlank())) {
                InventoryTransaction tx = new InventoryTransaction();
                tx.setIngredientId(id);
                tx.setTransactionType("EXPORT_WASTE");
                tx.setQuantity(hasStock ? ingredient.getCurrentStock() : BigDecimal.ZERO);
                tx.setPrice(BigDecimal.ZERO);
                tx.setReason("Xóa nguyên liệu: " + (reason != null && !reason.isBlank() ? reason : "Không có lý do"));
                transactionRepository.save(tx);
            }
            ingredient.setActive(false);
            ingredient.setCurrentStock(BigDecimal.ZERO);
            ingredientRepository.save(ingredient);
        }
    }

    // --- Stock Transactions ---

    @Transactional
    public InventoryTransaction importStock(StockTransactionRequest request) {
        Ingredient ingredient = getIngredientById(request.getIngredientId());
        
        ingredient.setCurrentStock(ingredient.getCurrentStock().add(request.getQuantity()));
        ingredientRepository.save(ingredient);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setIngredientId(request.getIngredientId());
        tx.setTransactionType("IMPORT");
        tx.setQuantity(request.getQuantity());
        tx.setPrice(request.getPrice());
        tx.setReason(request.getReason());
        return transactionRepository.save(tx);
    }

    @Transactional
    public InventoryTransaction exportStock(StockTransactionRequest request) {
        Ingredient ingredient = getIngredientById(request.getIngredientId());

        ingredient.setCurrentStock(ingredient.getCurrentStock().subtract(request.getQuantity()));
        ingredientRepository.save(ingredient);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setIngredientId(request.getIngredientId());
        tx.setTransactionType("EXPORT_WASTE");
        tx.setQuantity(request.getQuantity());
        tx.setPrice(BigDecimal.ZERO);
        tx.setReason(request.getReason());
        return transactionRepository.save(tx);
    }

    public List<InventoryTransaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<InventoryTransaction> getTransactions(LocalDate date, String period, String shift) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;

        if ("WEEK".equalsIgnoreCase(period)) {
            LocalDate weekStart = targetDate.with(DayOfWeek.MONDAY);
            start = weekStart.atStartOfDay();
            end = weekStart.plusDays(7).atStartOfDay();
        } else {
            start = targetDate.atStartOfDay();
            end = targetDate.plusDays(1).atStartOfDay();
        }

        List<InventoryTransaction> transactions = transactionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        if (shift == null || shift.isBlank() || "ALL".equalsIgnoreCase(shift) || "WEEK".equalsIgnoreCase(period)) {
            return transactions;
        }

        return transactions.stream()
                .filter(tx -> shift.equalsIgnoreCase(resolveShift(tx.getCreatedAt())))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getInventorySummary(LocalDate date, String period, String shift) {
        List<InventoryTransaction> transactions = getTransactions(date, period, shift);

        BigDecimal importQuantity = BigDecimal.ZERO;
        BigDecimal exportSaleQuantity = BigDecimal.ZERO;
        BigDecimal exportWasteQuantity = BigDecimal.ZERO;
        BigDecimal importValue = BigDecimal.ZERO;

        for (InventoryTransaction tx : transactions) {
            BigDecimal quantity = tx.getQuantity() != null ? tx.getQuantity() : BigDecimal.ZERO;
            BigDecimal price = tx.getPrice() != null ? tx.getPrice() : BigDecimal.ZERO;
            if ("IMPORT".equalsIgnoreCase(tx.getTransactionType())) {
                importQuantity = importQuantity.add(quantity);
                importValue = importValue.add(quantity.multiply(price));
            } else if ("EXPORT_SALE".equalsIgnoreCase(tx.getTransactionType())) {
                exportSaleQuantity = exportSaleQuantity.add(quantity);
            } else {
                exportWasteQuantity = exportWasteQuantity.add(quantity);
            }
        }

        Map<String, Long> countByType = transactions.stream()
                .collect(Collectors.groupingBy(InventoryTransaction::getTransactionType, Collectors.counting()));

        Map<UUID, Ingredient> ingredientById = ingredientRepository.findAll().stream()
                .collect(Collectors.toMap(Ingredient::getId, ingredient -> ingredient));

        Map<UUID, Map<String, BigDecimal>> summaryByIngredient = new HashMap<>();
        for (InventoryTransaction tx : transactions) {
            UUID ingredientId = tx.getIngredientId();
            BigDecimal quantity = tx.getQuantity() != null ? tx.getQuantity() : BigDecimal.ZERO;
            Map<String, BigDecimal> row = summaryByIngredient.computeIfAbsent(ingredientId, key -> {
                Map<String, BigDecimal> values = new HashMap<>();
                values.put("importQuantity", BigDecimal.ZERO);
                values.put("exportSaleQuantity", BigDecimal.ZERO);
                values.put("exportWasteQuantity", BigDecimal.ZERO);
                return values;
            });

            if ("IMPORT".equalsIgnoreCase(tx.getTransactionType())) {
                row.put("importQuantity", row.get("importQuantity").add(quantity));
            } else if ("EXPORT_SALE".equalsIgnoreCase(tx.getTransactionType())) {
                row.put("exportSaleQuantity", row.get("exportSaleQuantity").add(quantity));
            } else {
                row.put("exportWasteQuantity", row.get("exportWasteQuantity").add(quantity));
            }
        }

        List<Map<String, Object>> items = new ArrayList<>();
        summaryByIngredient.forEach((ingredientId, values) -> {
            Ingredient ingredient = ingredientById.get(ingredientId);
            BigDecimal importQty = values.get("importQuantity");
            BigDecimal exportSaleQty = values.get("exportSaleQuantity");
            BigDecimal exportWasteQty = values.get("exportWasteQuantity");
            items.add(Map.of(
                    "ingredientId", ingredientId,
                    "ingredientName", ingredient != null ? ingredient.getName() : "Nguyên liệu cũ",
                    "unit", ingredient != null ? ingredient.getUnit() : "",
                    "importQuantity", importQty,
                    "exportSaleQuantity", exportSaleQty,
                    "exportWasteQuantity", exportWasteQty,
                    "totalExportQuantity", exportSaleQty.add(exportWasteQty),
                    "netQuantity", importQty.subtract(exportSaleQty).subtract(exportWasteQty),
                    "currentStock", ingredient != null ? ingredient.getCurrentStock() : BigDecimal.ZERO
            ));
        });
        items.sort(Comparator.comparing(item -> String.valueOf(item.get("ingredientName"))));

        return Map.of(
                "totalTransactions", transactions.size(),
                "importQuantity", importQuantity,
                "exportSaleQuantity", exportSaleQuantity,
                "exportWasteQuantity", exportWasteQuantity,
                "totalExportQuantity", exportSaleQuantity.add(exportWasteQuantity),
                "importValue", importValue,
                "countByType", countByType,
                "items", items
        );
    }

    public String resolveShift(LocalDateTime time) {
        if (time == null) return "UNKNOWN";
        LocalTime current = time.toLocalTime();
        if (!current.isBefore(LocalTime.of(6, 0)) && current.isBefore(LocalTime.of(14, 0))) return "MORNING";
        if (!current.isBefore(LocalTime.of(14, 0)) && current.isBefore(LocalTime.of(22, 0))) return "AFTERNOON";
        return "NIGHT";
    }

    // --- Recipes Management ---

    public List<Recipe> getRecipesForMenuItem(UUID menuItemId) {
        return recipeRepository.findByMenuItemId(menuItemId);
    }

    @Transactional
    public List<Recipe> updateRecipeForMenuItem(UUID menuItemId, List<RecipeItemRequest> requests) {
        recipeRepository.deleteByMenuItemId(menuItemId);

        if (requests != null) {
            for (RecipeItemRequest req : requests) {
                Recipe recipe = new Recipe();
                recipe.setMenuItemId(menuItemId);
                recipe.setIngredientId(req.getIngredientId());
                recipe.setQuantityNeeded(req.getQuantityNeeded());
                recipeRepository.save(recipe);
            }
        }
        return recipeRepository.findByMenuItemId(menuItemId);
    }

    // --- Bulk Stock Deduction ---

    @Transactional
    public void deductStockForOrder(DeductStockRequest request) {
        if (request == null || request.getItems() == null) {
            return;
        }

        // First pass: validate stock
        java.util.Map<UUID, BigDecimal> totalRequired = new java.util.HashMap<>();
        for (DeductStockRequest.DeductItem item : request.getItems()) {
            List<Recipe> recipes = recipeRepository.findByMenuItemId(item.getMenuItemId());
            BigDecimal quantitySold = BigDecimal.valueOf(item.getQuantity());
            for (Recipe recipe : recipes) {
                BigDecimal needed = recipe.getQuantityNeeded().multiply(quantitySold);
                totalRequired.put(recipe.getIngredientId(), totalRequired.getOrDefault(recipe.getIngredientId(), BigDecimal.ZERO).add(needed));
            }
        }

        for (java.util.Map.Entry<UUID, BigDecimal> entry : totalRequired.entrySet()) {
            Ingredient ingredient = ingredientRepository.findById(entry.getKey()).orElse(null);
            if (ingredient != null && ingredient.isActive()) {
                if (ingredient.getCurrentStock().compareTo(entry.getValue()) < 0) {
                    throw new RuntimeException("Không đủ " + ingredient.getName() + " (Cần " + entry.getValue() + " " + ingredient.getUnit() + ", tồn kho " + ingredient.getCurrentStock() + " " + ingredient.getUnit() + ")");
                }
            }
        }

        for (DeductStockRequest.DeductItem item : request.getItems()) {
            List<Recipe> recipes = recipeRepository.findByMenuItemId(item.getMenuItemId());
            BigDecimal quantitySold = BigDecimal.valueOf(item.getQuantity());

            for (Recipe recipe : recipes) {
                Ingredient ingredient = ingredientRepository.findById(recipe.getIngredientId()).orElse(null);
                if (ingredient == null) {
                    continue;
                }

                BigDecimal totalDeducted = recipe.getQuantityNeeded().multiply(quantitySold);

                ingredient.setCurrentStock(ingredient.getCurrentStock().subtract(totalDeducted));
                ingredientRepository.save(ingredient);

                InventoryTransaction tx = new InventoryTransaction();
                tx.setIngredientId(ingredient.getId());
                tx.setTransactionType("EXPORT_SALE");
                tx.setQuantity(totalDeducted);
                tx.setPrice(BigDecimal.ZERO);
                tx.setReason("Xuất bán đơn hàng (Món UUID: " + item.getMenuItemId() + ")");
                transactionRepository.save(tx);
            }
        }
    }
}
