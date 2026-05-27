package com.vietnl.notificationservice.infrastructure.persistence.repositories;

import com.vietnl.notificationservice.domain.models.entities.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    List<DeviceToken> findByRole(String role);
    List<DeviceToken> findByRoleIn(List<String> roles);
    Optional<DeviceToken> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
}
