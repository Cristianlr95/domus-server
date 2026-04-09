package com.domus.server.audit.service;

import com.domus.server.audit.dto.response.AuditLogResponse;
import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.entity.AuditLogEntity;
import com.domus.server.audit.mapper.AuditLogMapper;
import com.domus.server.audit.repository.AuditLogRepository;
import com.domus.server.audit.support.AuditLogSpecifications;
import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditLogService(
        AuditLogRepository auditLogRepository,
        UserRepository userRepository,
        AuditLogMapper auditLogMapper,
        ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    public AuditLogResponse record(
        UUID actorUserId,
        String entityType,
        String entityId,
        AuditAction action,
        String summary,
        Object previousData,
        Object newData,
        Object contextData
    ) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setId(UUID.randomUUID());
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setSummary(summary);
        auditLog.setActorUser(resolveActor(actorUserId));
        auditLog.setPreviousData(serialize(previousData));
        auditLog.setNewData(serialize(newData));
        auditLog.setContextData(serialize(contextData));

        return auditLogMapper.toResponse(auditLogRepository.save(auditLog));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> list(
        UUID actorUserId,
        String entityType,
        AuditAction action,
        Instant from,
        Instant to,
        String search
    ) {
        Specification<AuditLogEntity> specification = AuditLogSpecifications.withActorUserId(actorUserId)
            .and(AuditLogSpecifications.withEntityType(entityType))
            .and(AuditLogSpecifications.withAction(action))
            .and(AuditLogSpecifications.occurredFrom(from))
            .and(AuditLogSpecifications.occurredTo(to))
            .and(AuditLogSpecifications.search(search));

        return auditLogRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "occurredAt"))
            .stream()
            .map(auditLogMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(UUID id) {
        return auditLogRepository.findById(id)
            .map(auditLogMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Audit log not found."));
    }

    private UserEntity resolveActor(UUID actorUserId) {
        if (actorUserId == null) {
            return null;
        }

        return userRepository.findById(actorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Audit actor user not found."));
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize audit payload.");
        }
    }
}
