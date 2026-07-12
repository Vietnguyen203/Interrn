package com.vietnl.orderservice.adapter.apis;

import com.vietnl.orderservice.application.reports.*;
import com.vietnl.orderservice.domain.models.entities.Order;
import com.vietnl.orderservice.infrastructure.communication.CatalogFeignClient;
import com.vietnl.orderservice.infrastructure.persistence.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/reports")
public class ReportAPI {

    private final OrderRepository orderRepository;
    private final com.vietnl.orderservice.infrastructure.communication.CatalogFeignClient catalogFeignClient;

    @Autowired
    public ReportAPI(OrderRepository orderRepository, CatalogFeignClient catalogFeignClient) {
        this.orderRepository = orderRepository;
        this.catalogFeignClient = catalogFeignClient;
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getReport(@RequestParam(defaultValue = "DAY") String type) {
        try {
            // Lấy các đơn hàng đã hoàn thành (COMPLETED)
            List<Order> allOrders = orderRepository.findByStatusIgnoreCase("COMPLETED");
            
            ReportStrategy strategy;
            
            // Strategy Selection Logic based on Type
            switch (type.toUpperCase()) {
                case "MONTH":
                    strategy = new RevenueByMonthStrategy();
                    break;
                case "WEEK":
                    strategy = new RevenueByWeekStrategy();
                    break;
                case "YEAR":
                    strategy = new RevenueByYearStrategy();
                    break;
                case "CATEGORY":
                    strategy = new RevenueByCategoryStrategy();
                    break;
                case "DAY":
                default:
                    strategy = new RevenueByDateStrategy();
                    break;
            }

            ReportEngine engine = new ReportEngine(strategy);
            List<Map<String, Object>> reportResult = engine.generate(allOrders);
            
            return ResponseEntity.ok(com.vietnl.orderservice.application.responses.ApiResponse.ok(reportResult));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(com.vietnl.orderservice.application.responses.ApiResponse.error("Lỗi tạo báo cáo: " + e.getMessage()));
        }
    }

    @GetMapping("/comparison")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getComparisonReport(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(defaultValue = "ALL") String shift,
            @RequestParam(defaultValue = "DAY") String period,
            @RequestHeader("Authorization") String token) {
        try {
            java.time.LocalDate targetDate = date != null ? date : java.time.LocalDate.now();
            java.time.LocalDateTime start;
            java.time.LocalDateTime end;

            if ("WEEK".equalsIgnoreCase(period)) {
                java.time.LocalDate weekStart = targetDate.with(java.time.DayOfWeek.MONDAY);
                start = weekStart.atStartOfDay();
                end = weekStart.plusDays(7).atStartOfDay();
            } else {
                start = targetDate.atStartOfDay();
                end = targetDate.plusDays(1).atStartOfDay();
            }

            // 1. Fetch completed orders in range
            List<Order> orders = orderRepository.findCompletedOrdersBetween(start, end);

            // 2. Filter by shift if applicable
            if (shift != null && !"ALL".equalsIgnoreCase(shift) && !"WEEK".equalsIgnoreCase(period)) {
                orders = orders.stream()
                        .filter(o -> shift.equalsIgnoreCase(resolveShift(o.getCreatedAt())))
                        .collect(java.util.stream.Collectors.toList());
            }

            // 3. Aggregate revenue data
            double totalRevenue = orders.stream()
                    .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0)
                    .sum();
            long orderCount = orders.size();
            double averageOrderValue = orderCount > 0 ? totalRevenue / orderCount : 0.0;

            // 4. Generate breakdown
            List<Map<String, Object>> breakdown = new java.util.ArrayList<>();
            if ("WEEK".equalsIgnoreCase(period)) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                Map<String, Double> dailyGroup = new java.util.LinkedHashMap<>();
                java.time.LocalDate currentDay = start.toLocalDate();
                for (int i = 0; i < 7; i++) {
                    dailyGroup.put(currentDay.format(formatter), 0.0);
                    currentDay = currentDay.plusDays(1);
                }
                for (Order o : orders) {
                    String dayStr = o.getCreatedAt().format(formatter);
                    double amount = o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0;
                    if (dailyGroup.containsKey(dayStr)) {
                        dailyGroup.put(dayStr, dailyGroup.get(dayStr) + amount);
                    }
                }
                dailyGroup.forEach((day, value) -> {
                    Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("name", day);
                    entry.put("value", value);
                    breakdown.add(entry);
                });
            } else {
                Map<String, Double> hourlyGroup = new java.util.TreeMap<>();
                for (int i = 0; i < 24; i++) {
                    hourlyGroup.put(String.format("%02d:00", i), 0.0);
                }
                for (Order o : orders) {
                    String hourStr = String.format("%02d:00", o.getCreatedAt().getHour());
                    double amount = o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0;
                    hourlyGroup.put(hourStr, hourlyGroup.getOrDefault(hourStr, 0.0) + amount);
                }
                hourlyGroup.forEach((hour, value) -> {
                    Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("name", hour);
                    entry.put("value", value);
                    breakdown.add(entry);
                });
            }

            // 5. Call catalog-service for inventory summary
            Map<String, Object> inventoryData = new java.util.HashMap<>();
            try {
                Map<String, Object> feignResponse = catalogFeignClient.getInventorySummary(
                        targetDate.toString(),
                        shift,
                        period,
                        token
                );
                if (feignResponse != null && feignResponse.containsKey("data")) {
                    inventoryData = (Map<String, Object>) feignResponse.get("data");
                }
            } catch (Exception feignEx) {
                inventoryData.put("error", "Không thể lấy dữ liệu kho: " + feignEx.getMessage());
            }

            // 6. Construct unified response
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("totalRevenue", java.math.BigDecimal.valueOf(totalRevenue).setScale(2, java.math.RoundingMode.HALF_UP));
            response.put("orderCount", orderCount);
            response.put("averageOrderValue", java.math.BigDecimal.valueOf(averageOrderValue).setScale(2, java.math.RoundingMode.HALF_UP));
            response.put("period", period);
            response.put("shift", shift);
            response.put("date", targetDate.toString());
            response.put("breakdown", breakdown);
            response.put("inventory", inventoryData);

            return ResponseEntity.ok(com.vietnl.orderservice.application.responses.ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(com.vietnl.orderservice.application.responses.ApiResponse.error("Lỗi tạo báo cáo đối soát: " + e.getMessage()));
        }
    }

    private String resolveShift(java.time.LocalDateTime time) {
        if (time == null) return "UNKNOWN";
        java.time.LocalTime current = time.toLocalTime();
        if (!current.isBefore(java.time.LocalTime.of(6, 0)) && current.isBefore(java.time.LocalTime.of(14, 0))) return "MORNING";
        if (!current.isBefore(java.time.LocalTime.of(14, 0)) && current.isBefore(java.time.LocalTime.of(22, 0))) return "AFTERNOON";
        return "NIGHT";
    }
}
