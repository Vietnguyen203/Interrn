package com.vietnl.orderservice.adapter.apis;

import com.vietnl.orderservice.domain.models.entities.Order;
import com.vietnl.orderservice.infrastructure.persistence.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderStatisticAPI {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderStatisticAPI(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ===== GET MOST FAVORITE FOOD BY MONTH =====
    // Endpoint: GET /orders/most-favorite-food?time=MM-yyyy
    @GetMapping("/most-favorite-food")
    public ResponseEntity<?> getMostFavoriteFood(@RequestParam("time") String time) {
        try {
            LocalDateTime[] bounds = getMonthBounds(time);
            List<Order> orders = orderRepository.findCompletedOrdersBetween(bounds[0], bounds[1]);

            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", "404", "message", "Not Found"));
            }

            Map<String, Integer> foodCounts = new HashMap<>();
            for (Order order : orders) {
                if (order.getItems() != null) {
                    for (var item : order.getItems()) {
                        String foodName = item.getFoodName();
                        if (foodName != null && !foodName.isEmpty()) {
                            foodCounts.put(foodName, foodCounts.getOrDefault(foodName, 0) + item.getQuantity());
                        }
                    }
                }
            }

            if (foodCounts.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", "404", "message", "Not Found"));
            }

            String mostFavorite = null;
            int maxCount = -1;
            for (Map.Entry<String, Integer> entry : foodCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostFavorite = entry.getKey();
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("foodName", mostFavorite);
            result.put("quantity", maxCount);

            return ResponseEntity.ok(Map.of(
                    "code", "200",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", "500", "message", e.getMessage()));
        }
    }

    // ===== GET LIST ORDER IN TIME (MONTH) =====
    // Endpoint: GET /orders/list?time=MM-yyyy
    @GetMapping("/list")
    public ResponseEntity<?> getListOrderInTime(@RequestParam("time") String time) {
        try {
            LocalDateTime[] bounds = getMonthBounds(time);
            List<Order> orders = orderRepository.findOrdersBetween(bounds[0], bounds[1]);

            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", "404", "message", "Not Found"));
            }

            return ResponseEntity.ok(Map.of(
                    "code", "200",
                    "data", orders.stream().map(order -> Map.of("data", order)).toList()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", "500", "message", e.getMessage()));
        }
    }

    private LocalDateTime[] getMonthBounds(String time) {
        try {
            // time format is "MM-yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date = LocalDate.parse("01-" + time, formatter);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusMonths(1).atStartOfDay();
            return new LocalDateTime[]{start, end};
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Expected MM-yyyy");
        }
    }
}
