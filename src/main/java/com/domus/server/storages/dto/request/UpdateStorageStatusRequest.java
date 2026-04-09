package com.domus.server.storages.dto.request;

import com.domus.server.storages.entity.StorageOccupancyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStorageStatusRequest(
    @NotNull
    Boolean active,

    @NotNull
    StorageOccupancyStatus occupancyStatus
) {
}
