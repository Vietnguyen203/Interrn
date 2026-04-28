package com.vietnl.sharedlibrary.observability.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEvent {
  private String resourceType;
  private String ipAddress;
  private String action;
  private String note;
  private String typeName;
  private String description;
  private boolean success;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long duration;

  private PayloadAuditLogEvent data;
}
