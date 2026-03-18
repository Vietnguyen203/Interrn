package com.manager.statistics.application.strategies.impl;

import com.manager.statistics.application.strategies.StatisticsStrategy;
import com.manager.order.domain.models.entities.Order;
import com.manager.order.domain.models.entities.OrderItem;
import com.manager.statistics.interfaces.rest.dto.StatisticsDTOs;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FoodPieChartStrategy implements StatisticsStrategy<StatisticsDTOs.FoodStatisticsResponse> {

    @Override
    public StatisticsDTOs.FoodStatisticsResponse calculate(List<Order> orders) {
        Map<String, Integer> foodCounts = new HashMap<>();
        int totalQuantity = 0;

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String foodName = item.getFoodName();
                int qty = item.getQuantity();
                foodCounts.put(foodName, foodCounts.getOrDefault(foodName, 0) + qty);
                totalQuantity += qty;
            }
        }

        final int finalTotalQuantity = totalQuantity;
        List<StatisticsDTOs.PieChartItem> items = foodCounts.entrySet().stream()
                .map(entry -> {
                    double percentage = finalTotalQuantity == 0 ? 0 : (entry.getValue() * 100.0 / finalTotalQuantity);
                    // Round to 2 decimal places
                    percentage = Math.round(percentage * 100.0) / 100.0;
                    return new StatisticsDTOs.PieChartItem(entry.getKey(), entry.getValue(), percentage);
                })
                .sorted((a, b) -> b.getCount() - a.getCount()) // Sort by count descending
                .collect(Collectors.toList());

        return new StatisticsDTOs.FoodStatisticsResponse(orders.size(), totalQuantity, items);
    }
}
