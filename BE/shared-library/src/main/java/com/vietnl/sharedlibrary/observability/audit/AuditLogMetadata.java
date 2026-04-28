package com.vietnl.sharedlibrary.observability.audit;

/** Interface chung để các Enum của service cung cấp thông tin cho Audit Log. */
public interface AuditLogMetadata {
  String getResourceType();

  String getAction();

  String getTypeName();

  String getDescription();
}
