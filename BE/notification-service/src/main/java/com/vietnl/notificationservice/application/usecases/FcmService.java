package com.vietnl.notificationservice.application.usecases;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.vietnl.notificationservice.domain.models.entities.DeviceToken;
import com.vietnl.notificationservice.infrastructure.persistence.repositories.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Gửi FCM push notification đến tất cả thiết bị theo role.
     * Nếu Firebase chưa được khởi tạo (placeholder key), bỏ qua.
     */
    public void sendToRole(String role, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("[FCM] Firebase chưa khởi tạo — bỏ qua push notification.");
            return;
        }

        try {
            List<DeviceToken> targets;
            if ("ALL".equals(role) || role == null) {
                targets = deviceTokenRepository.findAll();
            } else {
                // Gửi đến role cụ thể VÀ ALL
                targets = deviceTokenRepository.findByRoleIn(List.of(role, "ALL"));
            }

            if (targets.isEmpty()) {
                log.debug("[FCM] Không có device nào đăng ký role '{}'", role);
                return;
            }

            List<Message> messages = new ArrayList<>();
            for (DeviceToken token : targets) {
                Message.Builder msgBuilder = Message.builder()
                        .setToken(token.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setNotification(AndroidNotification.builder()
                                        .setSound("default")
                                        .setChannelId("food_order_channel")
                                        .build())
                                .build());

                if (data != null && !data.isEmpty()) {
                    msgBuilder.putAllData(data);
                }
                messages.add(msgBuilder.build());
            }

            BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages);
            log.info("[FCM] Gửi {} messages — Thành công: {}, Thất bại: {}",
                    messages.size(), response.getSuccessCount(), response.getFailureCount());

            // Xóa các token không hợp lệ (NOT_REGISTERED)
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    FirebaseMessagingException ex = responses.get(i).getException();
                    if (ex != null && ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                        String invalidToken = targets.get(i).getFcmToken();
                        deviceTokenRepository.deleteByFcmToken(invalidToken);
                        log.info("[FCM] Xóa token hết hạn: {}...", invalidToken.substring(0, Math.min(20, invalidToken.length())));
                    }
                }
            }
        } catch (Exception e) {
            log.error("[FCM] Lỗi khi gửi push notification: {}", e.getMessage());
        }
    }

    @Transactional
    public DeviceToken registerToken(String userId, String role, String fcmToken, String platform) {
        // Upsert: cập nhật nếu token đã tồn tại
        DeviceToken token = deviceTokenRepository.findByFcmToken(fcmToken)
                .orElse(new DeviceToken());
        token.setUserId(userId);
        token.setRole(role);
        token.setFcmToken(fcmToken);
        token.setPlatform(platform != null ? platform : "ANDROID");
        return deviceTokenRepository.save(token);
    }

    @Transactional
    public void removeToken(String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }
}
