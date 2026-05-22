package com.domus.server.admin.dto.response;

import com.domus.server.audit.entity.AuditAction;
import java.time.Instant;

public record AdminRecentActivityResponse(
    String entityType,
    String entityId,
    AuditAction action,
    String summary,
    String actorName,
    String actorEmail,
    Instant occurredAt
) {
}
