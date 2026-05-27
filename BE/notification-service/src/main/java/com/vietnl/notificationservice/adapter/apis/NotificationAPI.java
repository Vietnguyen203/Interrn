package com.vietnl.notificationservice.adapter.apis;

import com.vietnl.notificationservice.application.usecases.FcmService;
import com.vietnl.notificationservice.application.usecases.NotificationService;
import com.vietnl.notificationservice.domain.models.entities.DeviceToken;
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
    private final FcmService fcmService;

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
    public ResponseEntity<List<Notification>> getRecent(
            @RequestParam(defaultValue = "ALL") String role) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(role));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable String id) {
        return notificationService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === FCM Device Token Management ===

    @PostMapping("/device-token")
    public ResponseEntity<DeviceToken> registerToken(@RequestBody DeviceTokenRequest request) {
        DeviceToken saved = fcmService.registerToken(
                request.getUserId(),
                request.getRole(),
                request.getFcmToken(),
                request.getPlatform()
        );
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<Void> removeToken(@RequestBody DeviceTokenRequest request) {
        fcmService.removeToken(request.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    // === Inner DTOs ===

    @Data
    public static class NotificationRequest {
        private String title;
        private String message;
        private String type;          // success | error | info | warning
        private String recipientRole; // ALL | ADMIN | WAITER | KITCHEN
    }

    @Data
    public static class DeviceTokenRequest {
        private String userId;
        private String role;          // KITCHEN | WAITER | ADMIN
        private String fcmToken;
        private String platform;      // ANDROID | IOS
    }
}
