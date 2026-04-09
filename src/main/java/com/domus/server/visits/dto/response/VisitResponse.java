package com.domus.server.visits.dto.response;

import com.domus.server.visits.entity.VisitRegistrationType;
import com.domus.server.visits.entity.VisitStatus;
import java.time.Instant;
import java.util.UUID;

public record VisitResponse(
    UUID id,
    String visitorName,
    String visitorDocument,
    String visitorPhone,
    String vehiclePlate,
    String residentName,
    String unitLabel,
    String blockLabel,
    VisitStatus status,
    VisitRegistrationType registrationType,
    String observations,
    Instant entryAt,
    Instant exitAt,
    Instant createdAt,
    Instant updatedAt,
    VisitUserSummaryResponse residentUser,
    VisitUserSummaryResponse recordedByUser
) {
}
