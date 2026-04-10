package com.manager.statistics.interfaces.rest.api;

import com.manager.statistics.application.services.StatisticsService;
import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import com.manager.statistics.interfaces.rest.dto.StatisticsDTOs;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsAPI {

    private final StatisticsService statisticsService;

    private String resolveServer(HttpServletRequest request, String serverParam) {
        if (serverParam != null && !serverParam.isBlank())
            return serverParam;
        Object claimsObj = request.getAttribute("claims");
        if (claimsObj instanceof Claims) {
            Claims claims = (Claims) claimsObj;
            String fromToken = claims.get("server", String.class);
            if (fromToken != null && !fromToken.isBlank())
                return fromToken;
        }
        return "local"; // Default
    }

    // ─── Biểu đồ tròn: phân bổ món ăn ───────────────────────────────────────

    @GetMapping("/food-distribution")
    public BaseResponseDTO getFoodDistribution(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(defaultValue = "day") String type, // day, week, month
            @RequestParam(required = false) String date) { // yyyy-MM-dd

        String resolvedServer = resolveServer(request, server);
        StatisticsDTOs.FoodStatisticsResponse data = statisticsService.getFoodDistribution(resolvedServer, type, date);
        return new BaseResponseDTO("OK", "Success", data);
    }

    // ─── Biểu đồ doanh thu: bar / line chart ─────────────────────────────────

    /**
     * Trả về doanh thu và số đơn hàng theo từng điểm thời gian trong period.
     *
     * @param type "day" (24h) | "week" (Mon-Sun) | "month" (1-31 ngày)
     * @param date ngày tham chiếu yyyy-MM-dd, không truyền = hôm nay
     */
    @GetMapping("/revenue-chart")
    public BaseResponseDTO getRevenueChart(
            HttpServletRequest request,
            @RequestParam(required = false) String server,
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) String date) {

        String resolvedServer = resolveServer(request, server);
        StatisticsDTOs.RevenueChartResponse data = statisticsService.getRevenueChart(resolvedServer, type, date);
        return new BaseResponseDTO("OK", "Success", data);
    }
}
