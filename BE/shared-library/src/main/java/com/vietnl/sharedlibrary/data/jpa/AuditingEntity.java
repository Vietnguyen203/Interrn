package com.vietnl.sharedlibrary.data.jpa;

import com.eps.shared.core.utils.GenerationUtils;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Setter
@Getter
public class AuditingEntity {
  @Id private UUID id;

  @CreatedBy private String createdBy;
  @LastModifiedBy private String updatedBy;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;

  @PrePersist
  public void ensureId() {
    if (id == null) {
      id = GenerationUtils.randomUUID();
    }
  }
}
