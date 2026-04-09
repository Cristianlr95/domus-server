package com.domus.server.units.dto.response;

import com.domus.server.residents.entity.ResidentType;
import java.util.UUID;

public record UnitResidentSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String documentNumber,
    boolean active,
    ResidentType residentType
) {
}
