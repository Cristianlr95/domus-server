package com.domus.server.storages.mapper;

import com.domus.server.storages.dto.response.StorageResponse;
import com.domus.server.storages.dto.response.StorageUnitSummaryResponse;
import com.domus.server.storages.entity.StorageEntity;
import com.domus.server.units.entity.UnitEntity;
import org.springframework.stereotype.Component;

@Component
public class StorageMapper {

    public StorageResponse toResponse(StorageEntity storage) {
        return new StorageResponse(
            storage.getId(),
            storage.getStorageCode(),
            storage.getStorageType(),
            storage.getOccupancyStatus(),
            storage.isActive(),
            storage.getObservations(),
            storage.getCreatedAt(),
            storage.getUpdatedAt(),
            toUnitSummary(storage.getUnit())
        );
    }

    private StorageUnitSummaryResponse toUnitSummary(UnitEntity unit) {
        if (unit == null) {
            return null;
        }

        return new StorageUnitSummaryResponse(
            unit.getId(),
            unit.getUnitCode(),
            unit.getBlockLabel(),
            unit.getFloorNumber()
        );
    }
}
