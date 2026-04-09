package com.domus.server.storages.dto.response;

import java.util.UUID;

public record StorageUnitSummaryResponse(
    UUID id,
    String unitCode,
    String blockLabel,
    Integer floorNumber
) {
}
