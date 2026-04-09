package com.domus.server.parking.dto.response;

import com.domus.server.residents.entity.ResidentType;
import java.util.UUID;

public record ParkingResidentSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String documentNumber,
    ResidentType residentType,
    boolean active
) {
}
