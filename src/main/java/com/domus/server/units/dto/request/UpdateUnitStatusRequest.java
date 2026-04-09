package com.domus.server.units.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUnitStatusRequest(
    @NotNull
    Boolean active
) {
}
