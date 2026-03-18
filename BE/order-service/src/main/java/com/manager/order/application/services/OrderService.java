package com.manager.order.application.services;

import com.manager.order.interfaces.rest.dto.OrderDTOs;
import com.manager.order.interfaces.rest.dto.response.ReceiptDTO;
import com.manager.order.domain.models.entities.Order;
import com.manager.order.domain.models.entities.OrderItem;
import com.manager.order.domain.models.enums.OrderStatus;
import com.manager.order.domain.models.entities.Table;
import com.manager.order.infrastructure.persistence.jpa.OrderRepository;
import com.manager.order.infrastructure.persistence.jpa.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TableRepository tableRepository;

    /* NEW: đảm bảo mọi dòng món đều có orderItemId (id subdocument) */
    public Order ensureLineIds(Order order) {
        if (order == null || order.getItems() == null)
            return order;
        boolean mutated = false;
        for (OrderItem it : order.getItems()) {
            String lineId = it.getOrderItemId(); // ánh xạ sang id nội bộ
            if (lineId == null || lineId.isBlank()) {
                it.setOrderItemId(UUID.randomUUID().toString());
                mutated = true;
            }
        }
        if (mutated)
            orderRepository.save(order);
        return order;
    }

    private static double itemTotal(OrderItem i) {
        try {
            return i.getPrice() * i.getQuantity();
        } catch (NullPointerException e) {
            return 0d;
        }
    }

    private static double recomputeTotal(List<OrderItem> items) {
        return items.stream().mapToDouble(OrderService::itemTotal).sum();
    }

    @Transactional
    public ReceiptDTO checkout(String orderId, String server, OrderDTOs.CheckoutRequest req) {
        Order order = orderRepository.findByIdAndServer(orderId, server)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.ORDERING) {
            throw new RuntimeException("Order is not in ORDERING status");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order has no items");
        }

        // đảm bảo dòng có id trước khi chốt
        ensureLineIds(order);

        double subtotal = recomputeTotal(order.getItems());
        double discount = req.getDiscount() == null ? 0d : Math.max(0d, req.getDiscount());
        double total = Math.max(0d, subtotal - discount);

        Double amountReceived = req.getAmountReceived();
        if (amountReceived == null || amountReceived < total) {
            throw new RuntimeException("Insufficient amount");
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        Table table = tableRepository.findByIdAndServer(order.getTableId(), server).orElse(null);
        if (table != null && orderId.equals(table.getCurrentOrderId())) {
            table.setCurrentOrderId(null);
            tableRepository.save(table);
        }

        ReceiptDTO r = new ReceiptDTO();
        r.setOrderId(order.getId());
        r.setTableId(order.getTableId());
        r.setSubtotal(subtotal);
        r.setDiscount(discount);
        r.setTotal(total);
        r.setPaymentMethod(req.getPaymentMethod());
        r.setAmountReceived(amountReceived);
        r.setChange(amountReceived - total);
        r.setPaidAtEpochMs(System.currentTimeMillis());
        return r;
    }
}
