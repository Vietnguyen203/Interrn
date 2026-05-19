package com.vietnl.catalogservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "users-service", url = "${users.service.url}")
public interface UserFeignClient {

    @GetMapping("/users/{id}")
    Map<String, Object> getUserById(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String token
    );
}
