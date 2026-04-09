package com.domus.server.parking.dto.request;

import com.domus.server.parking.entity.ParkingOccupancyStatus;
import com.domus.server.parking.entity.ParkingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateParkingRequest(
    @NotBlank
    @Size(max = 50)
    String spotCode,

    @NotNull
    ParkingType parkingType,

    @NotNull
    ParkingOccupancyStatus occupancyStatus,

    UUID unitId,

    UUID residentId,

    @Size(max = 20)
    String vehiclePlate,

    @Size(max = 500)
    String observations
) {
}
