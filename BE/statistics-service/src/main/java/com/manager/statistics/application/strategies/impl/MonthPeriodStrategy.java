package com.manager.statistics.application.strategies.impl;

import com.manager.statistics.application.strategies.ChartPeriodStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy cho biểu đồ theo THÁNG.
 * Chia nhỏ thành N điểm dữ liệu (mỗi điểm = 1 ngày, N = số ngày trong tháng).
 * Label: "1", "2", ..., "28/29/30/31"
 */
@Component
public class MonthPeriodStrategy implements ChartPeriodStrategy {

    @Override
    public LocalDateTime getFrom(LocalDate date) {
        return date.withDayOfMonth(1).atStartOfDay();
    }

    @Override
    public LocalDateTime getTo(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);
    }

    @Override
    public List<String> getLabels(LocalDate date) {
        List<String> labels = new ArrayList<>();
        int daysInMonth = date.lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            labels.add(String.valueOf(d));
        }
        return labels;
    }

    @Override
    public LocalDateTime getPointStart(String label, LocalDate date) {
        int day = Integer.parseInt(label);
        return date.withDayOfMonth(day).atStartOfDay();
    }

    @Override
    public LocalDateTime getPointEnd(String label, LocalDate date) {
        int day = Integer.parseInt(label);
        return date.withDayOfMonth(day).atTime(23, 59, 59);
    }
}
