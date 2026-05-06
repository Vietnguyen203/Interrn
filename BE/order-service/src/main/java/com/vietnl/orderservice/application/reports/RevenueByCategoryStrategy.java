package com.vietnl.orderservice.application.reports;

import com.vietnl.orderservice.domain.models.entities.Order;
import com.vietnl.orderservice.domain.models.entities.OrderItem;
import java.util.*;
import java.util.stream.Collectors;

public class RevenueByCategoryStrategy implements ReportStrategy {
    @Override
    public List<Map<String, Object>> calculate(List<Order> orders) {
        Map<String, Double> aggregated = new HashMap<>();

        orders.stream()
            .forEach(order -> {
                for (OrderItem item : order.getItems()) {
                    if (item == null) continue;
                    String label = (item.getFoodName() != null) ? item.getFoodName() : "Món không tên";
                    double price = (item.getUnitPrice() != null) ? item.getUnitPrice().doubleValue() : 0.0;
                    
                    double itemTotal = price * item.getQuantity();
                    aggregated.put(label, aggregated.getOrDefault(label, 0.0) + itemTotal);
                }
            });

        return aggregated.entrySet().stream()
            .map(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", e.getKey());
                map.put("value", e.getValue());
                return map;
            })
            .collect(Collectors.toList());
    }
}
