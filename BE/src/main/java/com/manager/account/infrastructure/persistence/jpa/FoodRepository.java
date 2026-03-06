package com.manager.account.infrastructure.persistence.jpa;

import com.manager.account.domain.models.entities.Food;
import com.manager.account.domain.models.enums.FoodCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, String> {
    List<Food> findByServer(String server);

    Page<Food> findByServer(String server, Pageable pageable);

    Optional<Food> findByIdAndServer(String id, String server);

    Page<Food> findByServerAndCategoryAndFoodNameContainingIgnoreCase(String server, FoodCategory category, String q,
            Pageable pageable);

    Page<Food> findByServerAndCategory(String server, FoodCategory category, Pageable pageable);

    Page<Food> findByServerAndFoodNameContainingIgnoreCase(String server, String q, Pageable pageable);
}
