package com.manager.statistics.application.services;

import com.manager.statistics.application.strategies.ChartPeriodStrategy;
import com.manager.statistics.application.strategies.ChartStrategyFactory;
import com.manager.statistics.application.strategies.impl.FoodPieChartStrategy;
import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.statistics.infrastructure.clients.OrderServiceClient;
import com.manager.statistics.infrastructure.clients.dto.OrderResponseDTO;
import com.manager.statistics.interfaces.rest.dto.StatisticsDTOs;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final OrderServiceClient orderServiceClient;
    private final FoodPieChartStrategy foodPieChartStrategy;
    private final ChartStrategyFactory chartStrategyFactory;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    private List<OrderResponseDTO> fetchCompletedOrders(String server, LocalDateTime from, LocalDateTime to) {
        BaseResponseDTO response = orderServiceClient.getCompletedOrders(server, from.toString(), to.toString());
        if ("OK".equals(response.getCode()) && response.getData() != null) {
            List<Object> data = (List<Object>) response.getData();
            return data.stream()
                    .map(obj -> objectMapper.convertValue(obj, OrderResponseDTO.class))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    // ─── Pie chart: phân bổ món ăn ───────────────────────────────────────────

    public StatisticsDTOs.FoodStatisticsResponse getFoodDistribution(String server, String type, String dateStr) {
        LocalDateTime from;
        LocalDateTime to;

        LocalDate date = (dateStr == null || dateStr.isBlank()) ? LocalDate.now() : LocalDate.parse(dateStr);

        if ("day".equalsIgnoreCase(type)) {
            from = date.atStartOfDay();
            to = date.atTime(23, 59, 59);
        } else if ("week".equalsIgnoreCase(type)) {
            // Monday to Sunday
            from = date.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            to = date.with(java.time.DayOfWeek.SUNDAY).atTime(23, 59, 59);
        } else if ("month".equalsIgnoreCase(type)) {
            from = date.withDayOfMonth(1).atStartOfDay();
            to = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);
        } else {
            // Default to day if type is unknown
            from = date.atStartOfDay();
            to = date.atTime(23, 59, 59);
        }

        List<OrderResponseDTO> orders = fetchCompletedOrders(server, from, to);

        return foodPieChartStrategy.calculate(orders);
    }

    // ─── Bar / Line chart: doanh thu theo thời gian ──────────────────────────

    /**
     * Tính doanh thu và số đơn hàng cho từng điểm thời gian trong period.
     *
     * @param server  server name
     * @param type    "day" | "week" | "month"
     * @param dateStr ngày tham chiếu (yyyy-MM-dd), null → hôm nay
     * @return RevenueChartResponse
     */
    public StatisticsDTOs.RevenueChartResponse getRevenueChart(String server, String type, String dateStr) {
        LocalDate date = (dateStr == null || dateStr.isBlank()) ? LocalDate.now() : LocalDate.parse(dateStr);
        String resolvedType = (type == null || type.isBlank()) ? "day" : type.toLowerCase();

        ChartPeriodStrategy strategy = chartStrategyFactory.getStrategy(resolvedType);
        List<String> labels = strategy.getLabels(date);

        List<StatisticsDTOs.ChartDataPoint> dataPoints = new ArrayList<>();
        double totalRevenue = 0;
        long totalOrders = 0;

        for (String label : labels) {
            LocalDateTime pointStart = strategy.getPointStart(label, date);
            LocalDateTime pointEnd = strategy.getPointEnd(label, date);

            List<OrderResponseDTO> orders = fetchCompletedOrders(server, pointStart, pointEnd);

            double revenue = orders.stream().mapToDouble(OrderResponseDTO::getTotalAmount).sum();
            // Round to 2 decimal places
            revenue = Math.round(revenue * 100.0) / 100.0;

            dataPoints.add(new StatisticsDTOs.ChartDataPoint(label, revenue, orders.size()));
            totalRevenue += revenue;
            totalOrders += orders.size();
        }

        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;

        return new StatisticsDTOs.RevenueChartResponse(
                resolvedType,
                date.toString(),
                dataPoints,
                totalRevenue,
                totalOrders);
    }
}
