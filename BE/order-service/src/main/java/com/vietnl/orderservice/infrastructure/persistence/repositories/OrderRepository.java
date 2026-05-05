package com.vietnl.orderservice.infrastructure.persistence.repositories;

import com.vietnl.orderservice.domain.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByTableIdOrderByCreatedAtDesc(String tableId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
