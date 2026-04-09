package com.domus.server.parking.dto.request;

import com.domus.server.parking.entity.ParkingOccupancyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateParkingStatusRequest(
    @NotNull
    Boolean active,

    @NotNull
    ParkingOccupancyStatus occupancyStatus
) {
}
