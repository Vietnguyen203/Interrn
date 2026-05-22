package com.vietnl.catalogservice.infrastructure.persistence.repositories;

import com.vietnl.catalogservice.domain.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
}
