package com.manager.kitchen.infrastructure.clients;

import com.manager.common.interfaces.rest.dto.BaseResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "order-service", url = "${app.order-service.url:http://localhost:8083/foodordersystem/api}")
public interface OrderServiceClient {

    @GetMapping("/orders/items/pending")
    BaseResponseDTO getPendingItems(@RequestParam("server") String server);

    @PutMapping("/orders/items/{orderItemId}/status")
    BaseResponseDTO updateItemStatus(
            @PathVariable("orderItemId") String orderItemId,
            @RequestBody Map<String, String> body
    );
}
