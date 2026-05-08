package com.vietnl.orderservice.application.reports;

import com.vietnl.orderservice.domain.models.entities.Order;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RevenueByYearStrategy implements ReportStrategy {
    @Override
    public List<Map<String, Object>> calculate(List<Order> orders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        
        Map<String, Double> aggregated = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> (o.getCreatedAt() != null) ? o.getCreatedAt().format(formatter) : "Unknown",
                        TreeMap::new,
                        Collectors.summingDouble(o -> (o.getTotalAmount() != null) ? o.getTotalAmount().doubleValue() : 0.0)
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        aggregated.forEach((year, value) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", year);
            entry.put("value", value);
            result.add(entry);
        });
        
        return result;
    }
}
