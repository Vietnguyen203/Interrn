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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryTransactionRepository transactionRepository;

    // --- Ingredients CRUD ---

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
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

    public void deleteIngredient(UUID id) {
        ingredientRepository.deleteById(id);
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
