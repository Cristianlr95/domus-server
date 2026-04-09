package com.domus.server.parking.dto.response;

import java.util.UUID;

public record ParkingUnitSummaryResponse(
    UUID id,
    String unitCode,
    String blockLabel,
    Integer floorNumber
) {
}
