package com.vietnl.orderservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "table-service", url = "${table.service.url}")
public interface TableFeignClient {

    @PostMapping("/tables/{id}/assign-order")
    void assignOrder(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Server") String server
    );
}
