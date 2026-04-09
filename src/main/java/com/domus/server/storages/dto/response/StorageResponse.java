package com.domus.server.storages.dto.response;

import com.domus.server.storages.entity.StorageOccupancyStatus;
import com.domus.server.storages.entity.StorageType;
import java.time.Instant;
import java.util.UUID;

public record StorageResponse(
    UUID id,
    String storageCode,
    StorageType storageType,
    StorageOccupancyStatus occupancyStatus,
    boolean active,
    String observations,
    Instant createdAt,
    Instant updatedAt,
    StorageUnitSummaryResponse unit
) {
}
