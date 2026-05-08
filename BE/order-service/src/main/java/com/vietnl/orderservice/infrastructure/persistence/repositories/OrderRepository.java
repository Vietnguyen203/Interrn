package com.vietnl.orderservice.infrastructure.persistence.repositories;

import com.vietnl.orderservice.domain.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatusIgnoreCase(String status);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE UPPER(o.status) = UPPER(:status)")
    List<Order> findByStatusIgnoreCaseWithItems(@Param("status") String status);

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByTableIdOrderByCreatedAtDesc(String tableId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
