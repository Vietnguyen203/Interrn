package com.manager.statistics.application.strategies;

import com.manager.statistics.application.strategies.impl.DayPeriodStrategy;
import com.manager.statistics.application.strategies.impl.MonthPeriodStrategy;
import com.manager.statistics.application.strategies.impl.WeekPeriodStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory chọn ChartPeriodStrategy phù hợp dựa theo tham số type.
 * Sử dụng trong StatisticsService để tách biệt logic xác định khoảng thời gian
 * ra khỏi logic tính toán dữ liệu biểu đồ.
 *
 * Supported types: "day", "week", "month"
 */
@Component
@RequiredArgsConstructor
public class ChartStrategyFactory {

    private final DayPeriodStrategy dayPeriodStrategy;
    private final WeekPeriodStrategy weekPeriodStrategy;
    private final MonthPeriodStrategy monthPeriodStrategy;

    /**
     * Trả về strategy tương ứng với loại period.
     *
     * @param type "day" | "week" | "month" (không phân biệt hoa thường)
     * @return ChartPeriodStrategy phù hợp
     * @throws IllegalArgumentException nếu type không được hỗ trợ
     */
    public ChartPeriodStrategy getStrategy(String type) {
        if (type == null) {
            return dayPeriodStrategy;
        }
        switch (type.toLowerCase()) {
            case "day":
                return dayPeriodStrategy;
            case "week":
                return weekPeriodStrategy;
            case "month":
                return monthPeriodStrategy;
            default:
                throw new IllegalArgumentException(
                        "Unsupported chart period type: '" + type + "'. Use 'day', 'week', or 'month'.");
        }
    }
}
