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
    public ResponseEntity<?> getReport(@RequestParam(defaultValue = "DAY") String type) {
        try {
            // Sử dụng JPQL @Query + JOIN FETCH để lấy dữ liệu thực tế và tối ưu
            List<Order> allOrders = orderRepository.findByStatusWithItems("COMPLETED");
            
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
            return ResponseEntity.ok(engine.generate(allOrders));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi tạo báo cáo: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
