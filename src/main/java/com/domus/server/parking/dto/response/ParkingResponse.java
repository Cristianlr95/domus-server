package com.domus.server.parking.dto.response;

import com.domus.server.parking.entity.ParkingOccupancyStatus;
import com.domus.server.parking.entity.ParkingType;
import java.time.Instant;
import java.util.UUID;

public record ParkingResponse(
    UUID id,
    String spotCode,
    ParkingType parkingType,
    ParkingOccupancyStatus occupancyStatus,
    boolean active,
    String vehiclePlate,
    String observations,
    Instant createdAt,
    Instant updatedAt,
    ParkingUnitSummaryResponse unit,
    ParkingResidentSummaryResponse resident
) {
}
