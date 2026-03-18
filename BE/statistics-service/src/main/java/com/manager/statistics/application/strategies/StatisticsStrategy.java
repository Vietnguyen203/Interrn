package com.manager.statistics.application.strategies;

import com.manager.order.domain.models.entities.Order;

import java.util.List;

public interface StatisticsStrategy<T> {
    T calculate(List<Order> orders);
}
