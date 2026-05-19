package com.vietnl.orderservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "catalog-service", url = "${catalog.service.url}")
public interface CatalogFeignClient {

    @GetMapping("/catalog-service/items/{id}")
    Map<String, Object> getMenuItemById(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String token
    );
}
