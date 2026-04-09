package com.domus.server.residents.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateResidentStatusRequest(
    @NotNull
    Boolean active
) {
}
