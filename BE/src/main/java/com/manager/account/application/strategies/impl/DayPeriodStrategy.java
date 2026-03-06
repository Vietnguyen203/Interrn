package com.manager.account.application.strategies.impl;

import com.manager.account.application.strategies.ChartPeriodStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy cho biểu đồ theo NGÀY.
 * Chia nhỏ thành 24 điểm dữ liệu (mỗi điểm = 1 giờ).
 * Label: "00", "01", ..., "23"
 */
@Component
public class DayPeriodStrategy implements ChartPeriodStrategy {

    @Override
    public LocalDateTime getFrom(LocalDate date) {
        return date.atStartOfDay();
    }

    @Override
    public LocalDateTime getTo(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    @Override
    public List<String> getLabels(LocalDate date) {
        List<String> labels = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            labels.add(String.format("%02d", h));
        }
        return labels;
    }

    @Override
    public LocalDateTime getPointStart(String label, LocalDate date) {
        int hour = Integer.parseInt(label);
        return date.atTime(hour, 0, 0);
    }

    @Override
    public LocalDateTime getPointEnd(String label, LocalDate date) {
        int hour = Integer.parseInt(label);
        return date.atTime(hour, 59, 59);
    }
}
