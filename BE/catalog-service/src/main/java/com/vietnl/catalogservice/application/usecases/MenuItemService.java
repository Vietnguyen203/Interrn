package com.vietnl.catalogservice.application.usecases;

import com.vietnl.catalogservice.application.requests.MenuItemRequest;
import com.vietnl.catalogservice.domain.entities.MenuItem;
import com.vietnl.catalogservice.domain.enums.ExceptionMessage;
import com.vietnl.catalogservice.domain.enums.ItemStatus;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.CategoryRepository;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    public List<MenuItem> getAll() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> getByCategoryId(String categoryId) {
        return menuItemRepository.findByCategoryId(UUID.fromString(categoryId));
    }

    public MenuItem getById(String id) {
        return menuItemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.ITEM_NOT_FOUND.getMessage()));
    }

    public MenuItem create(MenuItemRequest request) {
        if (!StringUtils.hasText(request.getCode()))
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "code"));
        if (!StringUtils.hasText(request.getFoodName()))
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "foodName"));
        if (request.getCategoryId() == null)
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "categoryId"));
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException(ExceptionMessage.INVALID_PRICE.getMessage());

        // Validate category exists
        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        if (menuItemRepository.findByCode(request.getCode()).isPresent())
            throw new RuntimeException(ExceptionMessage.ITEM_CODE_DUPLICATE.getMessage());

        MenuItem item = new MenuItem();
        item.setCategoryId(request.getCategoryId());
        item.setCode(request.getCode());
        item.setFoodName(request.getFoodName());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl());
        item.setStatus(ItemStatus.ACTIVE.getValue());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        return menuItemRepository.save(item);
    }

    public MenuItem update(String id, MenuItemRequest request) {
        MenuItem item = getById(id);

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));
            item.setCategoryId(request.getCategoryId());
        }
        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(item.getCode())) {
            if (menuItemRepository.findByCode(request.getCode()).isPresent())
                throw new RuntimeException(ExceptionMessage.ITEM_CODE_DUPLICATE.getMessage());
            item.setCode(request.getCode());
        }
        if (StringUtils.hasText(request.getFoodName())) item.setFoodName(request.getFoodName());
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0) item.setPrice(request.getPrice());
        if (StringUtils.hasText(request.getImageUrl())) item.setImageUrl(request.getImageUrl());
        item.setUpdatedAt(LocalDateTime.now());

        return menuItemRepository.save(item);
    }

    public void delete(String id) {
        getById(id);
        menuItemRepository.deleteById(UUID.fromString(id));
    }
}
