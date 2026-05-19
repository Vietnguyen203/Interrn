package com.vietnl.usersservice.infrastructure.communication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${notification.service.url}")
public interface NotificationFeignClient {

    @PostMapping("/notifications/send")
    void sendNotification(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Authorization") String token
    );
}
