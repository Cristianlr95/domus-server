package com.domus.server.audit.dto.response;

import com.domus.server.audit.entity.AuditAction;
import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    String entityType,
    String entityId,
    AuditAction action,
    String summary,
    AuditActorResponse actor,
    Instant occurredAt,
    String previousData,
    String newData,
    String contextData
) {
}
