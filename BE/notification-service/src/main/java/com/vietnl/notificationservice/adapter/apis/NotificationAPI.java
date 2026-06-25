package com.vietnl.notificationservice.adapter.apis;

import com.vietnl.notificationservice.application.usecases.FcmService;
import com.vietnl.notificationservice.application.usecases.NotificationService;
import com.vietnl.notificationservice.domain.models.entities.DeviceToken;
import com.vietnl.notificationservice.domain.models.entities.Notification;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationAPI {

    private final NotificationService notificationService;
    private final FcmService fcmService;

    // POST /notifications/send - Gửi thông báo mới
    @PostMapping("/send")
    public ResponseEntity<Notification> send(
            Authentication auth,
            @RequestBody NotificationRequest request) {
        String username = auth != null ? auth.getName() : "system";
        Notification notification = notificationService.createAndSend(
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getRecipientRole(),
                username
        );
        return ResponseEntity.ok(notification);
    }

    // GET /notifications/recent - Lấy thông báo gần đây theo role
    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecent(
            @RequestParam(defaultValue = "ALL") String role,
            Authentication auth) {
        // Nếu client không truyền role, tự lấy role từ JWT
        String effectiveRole = (role.equals("ALL") && auth != null)
                ? extractRoleFromAuth(auth)
                : role;
        return ResponseEntity.ok(notificationService.getRecentNotifications(effectiveRole));
    }

    // PATCH /notifications/{id}/read - Đánh dấu đã đọc
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable String id) {
        return notificationService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === FCM Device Token Management ===

    // POST /notifications/device-token - Đăng ký FCM token thiết bị
    @PostMapping("/device-token")
    public ResponseEntity<DeviceToken> registerToken(
            Authentication auth,
            @RequestBody DeviceTokenRequest request) {
        // Nếu không có userId trong request, lấy từ Authentication
        String userId = (request.getUserId() != null && !request.getUserId().isBlank())
                ? request.getUserId()
                : (auth != null ? auth.getName() : null);
        DeviceToken saved = fcmService.registerToken(
                userId,
                request.getRole(),
                request.getFcmToken(),
                request.getPlatform()
        );
        return ResponseEntity.ok(saved);
    }

    // DELETE /notifications/device-token - Xóa FCM token thiết bị
    @DeleteMapping("/device-token")
    public ResponseEntity<Void> removeToken(@RequestBody DeviceTokenRequest request) {
        fcmService.removeToken(request.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    // === Helpers ===

    private String extractRoleFromAuth(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("ALL");
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
        private String platform;      // android | ios | web
    }
}
