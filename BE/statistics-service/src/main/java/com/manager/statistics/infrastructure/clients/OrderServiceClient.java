package com.manager.statistics.infrastructure.clients;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "${app.order-service.url:http://localhost:8083/foodordersystem/api}")
public interface OrderServiceClient {

    @GetMapping("/orders/completed")
    BaseResponseDTO getCompletedOrders(
            @RequestParam("server") String server,
            @RequestParam("from") String from,
            @RequestParam("to") String to
    );
}
