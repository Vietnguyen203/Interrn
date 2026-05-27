package com.vietnl.notificationservice.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@Slf4j
public class FcmConfig {

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            InputStream serviceAccount = resource.getInputStream();

            // Kiểm tra nếu file là placeholder (chứa _comment key)
            String content = new String(serviceAccount.readAllBytes());
            if (content.contains("_comment") || content.contains("YOUR_FIREBASE_PROJECT_ID")) {
                log.warn("[FCM] serviceAccountKey.json là file placeholder. FCM push notification sẽ bị tắt. " +
                         "Thay bằng key thật từ Firebase Console để bật tính năng này.");
                return;
            }

            // Re-read stream vì đã consumed
            serviceAccount = resource.getInputStream();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[FCM] FirebaseApp khởi tạo thành công.");
            }
        } catch (Exception e) {
            log.warn("[FCM] Không thể khởi tạo Firebase: {}. FCM push notification bị tắt.", e.getMessage());
        }
    }
}
