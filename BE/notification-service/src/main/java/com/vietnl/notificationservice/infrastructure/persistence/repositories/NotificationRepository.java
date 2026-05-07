package com.vietnl.notificationservice.infrastructure.persistence.repositories;

import com.vietnl.notificationservice.domain.models.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientRoleOrRecipientRoleOrderByCreatedAtDesc(String role, String all);
}
