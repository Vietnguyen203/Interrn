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
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
    java.util.Optional<Order> findById(UUID id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"items"})
    List<Order> findByTableIdAndStatusIn(String tableId, List<String> statuses);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE UPPER(o.status) = UPPER(:status)")
    List<Order> findByStatusIgnoreCaseWithItems(@Param("status") String status);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE UPPER(o.status) = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end")
    List<Order> findCompletedOrdersBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findOrdersBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    List<Order> findByTableIdOrderByCreatedAtDesc(String tableId);
    
    long countByTableIdAndStatusIn(String tableId, List<String> statuses);

    List<Order> findAllByOrderByCreatedAtDesc();
}
