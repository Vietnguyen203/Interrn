package com.manager.statistics.application.strategies;

import com.manager.statistics.infrastructure.clients.dto.OrderResponseDTO;

import java.util.List;

public interface StatisticsStrategy<T> {
    T calculate(List<OrderResponseDTO> orders);
}
