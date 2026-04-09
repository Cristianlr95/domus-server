package com.domus.server.residents.dto.response;

import java.util.UUID;

public record ResidentUnitSummaryResponse(
    UUID id,
    String unitCode,
    String blockLabel,
    Integer floorNumber
) {
}
