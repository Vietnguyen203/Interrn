package com.vietnl.paymentservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderFeignClient {

    @GetMapping("/orders/{id}")
    Map<String, Object> getOrderById(
            @PathVariable("id") UUID id,
            @RequestHeader("Authorization") String token
    );

    @PatchMapping("/orders/{id}/status")
    Map<String, Object> updateOrderStatus(
            @PathVariable("id") UUID id,
            @RequestParam("status") String status,
            @RequestHeader("Authorization") String token
    );
}
