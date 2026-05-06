package com.vietnl.orderservice.application.reports;

import com.vietnl.orderservice.domain.models.entities.Order;
import java.util.List;
import java.util.Map;

public class ReportEngine {
    private ReportStrategy strategy;

    public ReportEngine(ReportStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ReportStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Map<String, Object>> generate(List<Order> orders) {
        if (strategy == null) {
            throw new IllegalStateException("Strategy not set");
        }
        return strategy.calculate(orders);
    }
}
