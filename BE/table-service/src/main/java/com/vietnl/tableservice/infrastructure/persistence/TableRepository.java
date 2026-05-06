package com.vietnl.tableservice.infrastructure.persistence;

import com.vietnl.tableservice.domain.entities.RestaurantTable;
import com.vietnl.tableservice.domain.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, UUID> {
    List<RestaurantTable> findByStatus(TableStatus status);
    Optional<RestaurantTable> findByTableNumber(Integer tableNumber);
    boolean existsByTableNumber(Integer tableNumber);
}
