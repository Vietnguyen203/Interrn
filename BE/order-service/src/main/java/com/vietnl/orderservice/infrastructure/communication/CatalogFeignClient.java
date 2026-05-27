package com.vietnl.orderservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogFeignClient {

    @GetMapping("/catalog-service/items/{id}")
    Map<String, Object> getMenuItemById(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String token
    );

    @PostMapping("/catalog-service/inventory/deduct")
    void deductStock(
            @RequestBody Map<String, Object> deductRequest,
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/catalog-service/inventory/transactions/summary")
    Map<String, Object> getInventorySummary(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam("shift") String shift,
            @RequestParam("period") String period,
            @RequestHeader("Authorization") String token
    );
}
