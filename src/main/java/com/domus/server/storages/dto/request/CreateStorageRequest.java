package com.domus.server.storages.dto.request;

import com.domus.server.storages.entity.StorageOccupancyStatus;
import com.domus.server.storages.entity.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStorageRequest(
    @NotBlank
    @Size(max = 50)
    String storageCode,

    @NotNull
    StorageType storageType,

    @NotNull
    StorageOccupancyStatus occupancyStatus,

    @NotNull
    UUID unitId,

    @Size(max = 500)
    String observations
) {
}
