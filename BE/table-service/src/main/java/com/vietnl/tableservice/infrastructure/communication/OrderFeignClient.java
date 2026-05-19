package com.vietnl.tableservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderFeignClient {

    @GetMapping("/orders/tables/{tableId}/creator")
    Map<String, Object> getCreatorByTable(
            @PathVariable("tableId") String tableId,
            @RequestHeader("Authorization") String token
    );
}
