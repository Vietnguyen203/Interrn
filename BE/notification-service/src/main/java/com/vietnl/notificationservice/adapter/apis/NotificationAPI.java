package com.vietnl.notificationservice.adapter.apis;

import com.vietnl.notificationservice.application.usecases.NotificationService;
import com.vietnl.notificationservice.domain.models.entities.Notification;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationAPI {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Notification> send(@RequestBody NotificationRequest request) {
        Notification notification = notificationService.createAndSend(
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getRecipientRole()
        );
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecent(@RequestParam(defaultValue = "ALL") String role) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(role));
    }

    @Data
    public static class NotificationRequest {
        private String title;
        private String message;
        private String type; // success, error, info, warning
        private String recipientRole; // ALL, ADMIN, WAITER, KITCHEN
    }
}
