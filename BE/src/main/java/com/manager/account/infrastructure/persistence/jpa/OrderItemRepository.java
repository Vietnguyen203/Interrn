package com.manager.account.infrastructure.persistence.jpa;

import com.manager.account.domain.models.entities.OrderItem;
import com.manager.account.domain.models.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderServerAndStatusIn(String server, List<OrderItemStatus> statuses);

    Optional<OrderItem> findByOrderItemIdAndOrderServer(String orderItemId, String server);
}
