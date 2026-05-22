package com.vietnl.catalogservice.infrastructure.persistence.repositories;

import com.vietnl.catalogservice.domain.entities.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {
    List<Recipe> findByMenuItemId(UUID menuItemId);
    void deleteByMenuItemId(UUID menuItemId);
}
