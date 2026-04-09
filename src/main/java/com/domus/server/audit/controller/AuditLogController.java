package com.domus.server.audit.controller;

import com.domus.server.audit.dto.response.AuditLogResponse;
import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@PreAuthorize("hasAuthority('audit.read')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List audit logs with optional filters")
    public ApiResponse<List<AuditLogResponse>> list(
        @RequestParam(required = false) UUID actorUserId,
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) AuditAction action,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(auditLogService.list(actorUserId, entityType, action, from, to, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log detail")
    public ApiResponse<AuditLogResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(auditLogService.getById(id));
    }
}
