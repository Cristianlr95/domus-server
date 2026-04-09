package com.domus.server.audit.dto.response;

import java.util.UUID;

public record AuditActorResponse(
    UUID id,
    String firstName,
    String lastName,
    String email
) {
}
