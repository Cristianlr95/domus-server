package com.domus.server.storages.support;

import com.domus.server.storages.entity.StorageEntity;
import com.domus.server.storages.entity.StorageOccupancyStatus;
import org.springframework.data.jpa.domain.Specification;

public final class StorageSpecifications {

    private StorageSpecifications() {
    }

    public static Specification<StorageEntity> withActive(Boolean active) {
        return (root, query, builder) ->
            active == null ? builder.conjunction() : builder.equal(root.get("active"), active);
    }

    public static Specification<StorageEntity> withOccupancyStatus(StorageOccupancyStatus occupancyStatus) {
        return (root, query, builder) ->
            occupancyStatus == null ? builder.conjunction() : builder.equal(root.get("occupancyStatus"), occupancyStatus);
    }

    public static Specification<StorageEntity> withUnitId(java.util.UUID unitId) {
        return (root, query, builder) ->
            unitId == null ? builder.conjunction() : builder.equal(root.get("unit").get("id"), unitId);
    }

    public static Specification<StorageEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";

            return builder.or(
                builder.like(builder.lower(root.get("storageCode")), like),
                builder.like(builder.lower(root.get("observations")), like),
                builder.like(builder.lower(root.get("unit").get("unitCode")), like),
                builder.like(builder.lower(root.get("unit").get("blockLabel")), like)
            );
        };
    }
}
