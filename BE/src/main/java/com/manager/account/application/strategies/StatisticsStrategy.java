package com.manager.account.application.strategies;

import com.manager.account.domain.models.entities.Order;

import java.util.List;

public interface StatisticsStrategy<T> {
    T calculate(List<Order> orders);
}
