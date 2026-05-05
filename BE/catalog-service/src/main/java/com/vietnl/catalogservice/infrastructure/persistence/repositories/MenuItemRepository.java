package com.vietnl.catalogservice.infrastructure.persistence.repositories;

import com.vietnl.catalogservice.domain.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByCategoryId(UUID categoryId);
    Optional<MenuItem> findByCode(String code);
}
