package com.manager.order.infrastructure.persistence.jpa;

import com.manager.order.domain.models.entities.Order;
import com.manager.common.domain.models.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

        Optional<Order> findByIdAndServer(String id, String server);

        List<Order> findByServer(String server);

        Page<Order> findByServer(String server, Pageable pageable);

        List<Order> findByServerAndStatus(String server, OrderStatus status);

        Page<Order> findByServerAndStatus(String server, OrderStatus status, Pageable pageable);

        List<Order> findByCreatedBy(String createdBy);

        List<Order> findByServerAndCreatedAtBetween(String server, LocalDateTime start, LocalDateTime end);

        List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT o FROM Order o WHERE o.server = :server AND o.tableId = :tableId AND o.status <> :status")
        List<Order> findByServerAndTableIdAndStatusNot(@Param("server") String server, @Param("tableId") String tableId,
                        @Param("status") OrderStatus status);

        List<Order> findByServerAndStatusAndCreatedAtBetween(String server, OrderStatus status, LocalDateTime start,
                        LocalDateTime end);

        Page<Order> findAllByServer(String server, Pageable pageable);

        Page<Order> findAllByServerAndCreatedBy(String server, String createdBy, Pageable pageable);
}
