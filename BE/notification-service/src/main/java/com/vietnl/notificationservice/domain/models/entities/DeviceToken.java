package com.vietnl.notificationservice.domain.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_tokens",
    uniqueConstraints = @UniqueConstraint(columnNames = {"fcm_token"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceToken extends AuditingEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "role", nullable = false)
    private String role;  // KITCHEN, WAITER, ADMIN, ALL

    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    @Column(name = "platform")
    private String platform;  // ANDROID, IOS
}
