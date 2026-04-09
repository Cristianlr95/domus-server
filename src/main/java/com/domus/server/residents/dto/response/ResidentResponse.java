package com.domus.server.residents.dto.response;

import com.domus.server.residents.entity.ResidentType;
import java.time.Instant;
import java.util.UUID;

public record ResidentResponse(
    UUID id,
    String firstName,
    String lastName,
    String documentNumber,
    String email,
    String phone,
    boolean active,
    ResidentType residentType,
    ResidentUnitSummaryResponse unit,
    Instant createdAt,
    Instant updatedAt,
    ResidentLinkedUserResponse linkedUser
) {
}
