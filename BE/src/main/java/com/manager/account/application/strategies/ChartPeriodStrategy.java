package com.manager.account.application.strategies;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Strategy interface cho việc xác định khoảng thời gian của biểu đồ.
 * Mỗi concrete strategy tương ứng với một loại period: day, week, month.
 */
public interface ChartPeriodStrategy {

    /** Thời điểm bắt đầu của toàn bộ period */
    LocalDateTime getFrom(LocalDate date);

    /** Thời điểm kết thúc của toàn bộ period */
    LocalDateTime getTo(LocalDate date);

    /**
     * Danh sách label cho từng điểm dữ liệu trên trục X.
     * Ví dụ: day → ["00","01",...,"23"], week → ["Mon","Tue",...,"Sun"]
     */
    List<String> getLabels(LocalDate date);

    /** Thời điểm bắt đầu của một điểm dữ liệu cụ thể */
    LocalDateTime getPointStart(String label, LocalDate date);

    /** Thời điểm kết thúc của một điểm dữ liệu cụ thể */
    LocalDateTime getPointEnd(String label, LocalDate date);
}
