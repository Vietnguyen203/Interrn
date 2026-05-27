package com.vietnl.catalogservice.infrastructure.persistence.repositories;

import com.vietnl.catalogservice.domain.entities.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    List<InventoryTransaction> findByIngredientIdOrderByCreatedAtDesc(UUID ingredientId);
    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();
    List<InventoryTransaction> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}
