package com.vietnl.notificationservice.application.usecases;

import com.vietnl.notificationservice.domain.models.entities.Notification;
import com.vietnl.notificationservice.infrastructure.persistence.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;

    @Transactional
    public Notification createAndSend(String title, String message, String type, String role, String createdBy) {
        // 1. Lưu vào Database
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipientRole(role);
        notification.setCreatedBy(createdBy);
        notification = repository.save(notification);

        // 2. Gửi qua WebSocket STOMP
        messagingTemplate.convertAndSend("/topic/public", notification);
        if (role != null && !role.equals("ALL")) {
            messagingTemplate.convertAndSend("/topic/role/" + role, notification);
        }

        // 3. Gửi Firebase FCM push notification
        Map<String, String> data = Map.of(
            "notificationId", notification.getId().toString(),
            "type", type != null ? type : "info",
            "role", role != null ? role : "ALL"
        );
        fcmService.sendToRole(role, title, message, data);

        return notification;
    }

    public List<Notification> getRecentNotifications(String role) {
        return repository.findByRecipientRoleOrRecipientRoleOrderByCreatedAtDesc(role, "ALL");
    }

    @Transactional
    public Optional<Notification> markAsRead(String id) {
        return repository.findById(UUID.fromString(id)).map(n -> {
            n.setRead(true);
            return repository.save(n);
        });
    }
}
