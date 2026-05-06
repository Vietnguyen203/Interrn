package com.vietnl.orderservice.application.reports;

import com.vietnl.orderservice.domain.models.entities.Order;
import java.util.List;
import java.util.Map;

public interface ReportStrategy {
    List<Map<String, Object>> calculate(List<Order> orders);
}
