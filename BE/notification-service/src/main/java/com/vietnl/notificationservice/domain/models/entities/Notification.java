package com.vietnl.notificationservice.domain.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends AuditingEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "type")
    private String type; // success, error, info, warning

    @Column(name = "recipient_role")
    private String recipientRole; // ALL, ADMIN, WAITER, KITCHEN

    @Column(name = "is_read")
    private boolean read = false;
}
