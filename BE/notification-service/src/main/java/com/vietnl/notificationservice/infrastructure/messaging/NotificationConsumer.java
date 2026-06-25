package com.vietnl.notificationservice.infrastructure.messaging;

import com.vietnl.notificationservice.application.usecases.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notifications-topic", groupId = "notification-group")
    public void consume(Map<String, Object> message) {
        log.info("Received message from Kafka: {}", message);
        try {
            String title = (String) message.get("title");
            String content = (String) message.get("message");
            String type = (String) message.get("type");
            String role = (String) message.get("recipientRole");

            notificationService.createAndSend(title, content, type, role, "system");
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
        }
    }
}
