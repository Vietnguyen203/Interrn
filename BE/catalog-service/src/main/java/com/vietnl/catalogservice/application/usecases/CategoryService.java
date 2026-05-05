package com.vietnl.catalogservice.application.usecases;

import com.vietnl.catalogservice.application.requests.CategoryRequest;
import com.vietnl.catalogservice.domain.entities.Category;
import com.vietnl.catalogservice.domain.enums.CategoryStatus;
import com.vietnl.catalogservice.domain.enums.ExceptionMessage;
import com.vietnl.catalogservice.infrastructure.persistence.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(String id) {
        return categoryRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));
    }

    public Category create(CategoryRequest request) {
        if (!StringUtils.hasText(request.getCode()))
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "code"));
        if (!StringUtils.hasText(request.getName()))
            throw new RuntimeException(String.format(ExceptionMessage.MISSING_REQUIRED_FIELD.getMessage(), "name"));

        if (categoryRepository.findByCode(request.getCode()).isPresent())
            throw new RuntimeException(ExceptionMessage.CATEGORY_CODE_DUPLICATE.getMessage());

        Category category = new Category();
        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(CategoryStatus.ACTIVE.getValue());

        return categoryRepository.save(category);
    }

    public Category update(String id, CategoryRequest request) {
        Category category = getById(id);

        if (StringUtils.hasText(request.getCode()) && !request.getCode().equals(category.getCode())) {
            if (categoryRepository.findByCode(request.getCode()).isPresent())
                throw new RuntimeException(ExceptionMessage.CATEGORY_CODE_DUPLICATE.getMessage());
            category.setCode(request.getCode());
        }
        if (StringUtils.hasText(request.getName())) category.setName(request.getName());
        if (StringUtils.hasText(request.getDescription())) category.setDescription(request.getDescription());

        return categoryRepository.save(category);
    }

    public void delete(String id) {
        getById(id);
        categoryRepository.deleteById(UUID.fromString(id));
    }
}
