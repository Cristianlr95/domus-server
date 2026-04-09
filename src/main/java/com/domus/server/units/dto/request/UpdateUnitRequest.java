package com.domus.server.units.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdateUnitRequest(
    @NotBlank
    @Size(max = 50)
    String unitCode,

    @NotBlank
    @Size(max = 80)
    String blockLabel,

    Integer floorNumber,

    @Size(max = 500)
    String observations,

    List<UUID> residentIds
) {
}
