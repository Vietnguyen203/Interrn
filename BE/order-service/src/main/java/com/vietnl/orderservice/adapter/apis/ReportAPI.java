package com.vietnl.orderservice.adapter.apis;

import com.vietnl.orderservice.application.reports.*;
import com.vietnl.orderservice.domain.models.entities.Order;
import com.vietnl.orderservice.infrastructure.persistence.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*") // Cho phép gọi từ Frontend khác port
public class ReportAPI {

    private final OrderRepository orderRepository;

    @Autowired
    public ReportAPI(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
}
