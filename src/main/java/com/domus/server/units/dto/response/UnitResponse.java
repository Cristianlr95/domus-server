package com.domus.server.units.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UnitResponse(
    UUID id,
    String unitCode,
    String blockLabel,
    Integer floorNumber,
    boolean active,
    String observations,
    Instant createdAt,
    Instant updatedAt,
    List<UnitResidentSummaryResponse> residents
) {
}
