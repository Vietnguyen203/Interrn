package com.vietnl.sharedlibrary.observability.audit;

public interface AuditLogPort {
    void log(AuditLogEvent event);
}
