package com.manager.statistics.application.strategies.impl;

import com.manager.statistics.application.strategies.ChartPeriodStrategy;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Strategy cho biểu đồ theo TUẦN.
 * Chia nhỏ thành 7 điểm dữ liệu (Thứ 2 → Chủ Nhật).
 * Label: "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
 */
@Component
public class WeekPeriodStrategy implements ChartPeriodStrategy {

    private static final List<String> LABELS = Arrays.asList(
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    @Override
    public LocalDateTime getFrom(LocalDate date) {
        return date.with(DayOfWeek.MONDAY).atStartOfDay();
    }

    @Override
    public LocalDateTime getTo(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
    }

    @Override
    public List<String> getLabels(LocalDate date) {
        return LABELS;
    }

    @Override
    public LocalDateTime getPointStart(String label, LocalDate date) {
        int idx = LABELS.indexOf(label);
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        return monday.plusDays(idx).atStartOfDay();
    }

    @Override
    public LocalDateTime getPointEnd(String label, LocalDate date) {
        int idx = LABELS.indexOf(label);
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        return monday.plusDays(idx).atTime(23, 59, 59);
    }
}
