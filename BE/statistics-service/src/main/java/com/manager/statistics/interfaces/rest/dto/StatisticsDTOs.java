package com.manager.statistics.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class StatisticsDTOs {

    // ─── Biểu đồ tròn (Pie chart) ────────────────────────────────────────────

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PieChartItem {
        private String name;
        private int count;
        private double percentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodStatisticsResponse {
        private long totalOrders;
        private int totalItemsOrdered;
        private List<PieChartItem> items;
    }

    // ─── Biểu đồ doanh thu (Bar / Line chart) ────────────────────────────────

    /**
     * Một điểm dữ liệu trên trục X của biểu đồ doanh thu.
     * - day → label = "00" .. "23" (giờ)
     * - week → label = "Mon" .. "Sun"
     * - month → label = "1" .. "31"
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartDataPoint {
        private String label;
        private double revenue;
        private long orderCount;
    }

    /**
     * Response cho endpoint /statistics/revenue-chart.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueChartResponse {
        /** Loại period: "day" | "week" | "month" */
        private String period;
        /** Ngày tham chiếu (input date) */
        private String date;
        /** Danh sách các điểm dữ liệu */
        private List<ChartDataPoint> data;
        /** Tổng doanh thu trong period */
        private double totalRevenue;
        /** Tổng số đơn hàng trong period */
        private long totalOrders;
    }
}
