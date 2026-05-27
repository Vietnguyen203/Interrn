package com.vietnl.orderservice.application.reports;

import com.vietnl.orderservice.domain.models.entities.Order;

import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RevenueByWeekStrategy implements ReportStrategy {
    @Override
    public List<Map<String, Object>> calculate(List<Order> orders) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        Map<String, Double> aggregated = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            if (order.getCreatedAt() == null) return "Unknown";
                            int week = order.getCreatedAt().get(weekFields.weekOfWeekBasedYear());
                            int year = order.getCreatedAt().get(weekFields.weekBasedYear());
                            return String.format("%d-W%02d", year, week);
                        },
                        TreeMap::new,
                        Collectors.summingDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        aggregated.forEach((week, value) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", week);
            entry.put("value", value);
            result.add(entry);
        });
        return result;
    }
}
