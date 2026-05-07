package com.vietnl.notificationservice.application.usecases;

import com.vietnl.notificationservice.domain.models.entities.Notification;
import com.vietnl.notificationservice.infrastructure.persistence.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createAndSend(String title, String message, String type, String role) {
        // 1. Lưu vào Database
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipientRole(role);
        notification = repository.save(notification);

        // 2. Gửi qua WebSocket
        // Gửi đến topic chung cho tất cả
        messagingTemplate.convertAndSend("/topic/public", notification);
        
        // Gửi đến topic riêng cho từng role nếu cần
        if (role != null && !role.equals("ALL")) {
            messagingTemplate.convertAndSend("/topic/role/" + role, notification);
        }

        return notification;
    }

    public List<Notification> getRecentNotifications(String role) {
        return repository.findByRecipientRoleOrRecipientRoleOrderByCreatedAtDesc(role, "ALL");
    }
}
