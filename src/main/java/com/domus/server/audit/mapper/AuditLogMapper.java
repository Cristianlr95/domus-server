package com.domus.server.audit.mapper;

import com.domus.server.audit.dto.response.AuditActorResponse;
import com.domus.server.audit.dto.response.AuditLogResponse;
import com.domus.server.audit.entity.AuditLogEntity;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLogEntity auditLog) {
        return new AuditLogResponse(
            auditLog.getId(),
            auditLog.getEntityType(),
            auditLog.getEntityId(),
            auditLog.getAction(),
            auditLog.getSummary(),
            toActorResponse(auditLog.getActorUser()),
            auditLog.getOccurredAt(),
            auditLog.getPreviousData(),
            auditLog.getNewData(),
            auditLog.getContextData()
        );
    }

    private AuditActorResponse toActorResponse(UserEntity actorUser) {
        if (actorUser == null) {
            return null;
        }

        return new AuditActorResponse(
            actorUser.getId(),
            actorUser.getFirstName(),
            actorUser.getLastName(),
            actorUser.getEmail()
        );
    }
}
