package com.domus.server.parking.support;

import com.domus.server.parking.entity.ParkingEntity;
import com.domus.server.parking.entity.ParkingOccupancyStatus;
import com.domus.server.parking.entity.ParkingType;
import org.springframework.data.jpa.domain.Specification;

public final class ParkingSpecifications {

    private ParkingSpecifications() {
    }

    public static Specification<ParkingEntity> withActive(Boolean active) {
        return (root, query, builder) ->
            active == null ? builder.conjunction() : builder.equal(root.get("active"), active);
    }

    public static Specification<ParkingEntity> withOccupancyStatus(ParkingOccupancyStatus occupancyStatus) {
        return (root, query, builder) ->
            occupancyStatus == null ? builder.conjunction() : builder.equal(root.get("occupancyStatus"), occupancyStatus);
    }

    public static Specification<ParkingEntity> withType(ParkingType parkingType) {
        return (root, query, builder) ->
            parkingType == null ? builder.conjunction() : builder.equal(root.get("parkingType"), parkingType);
    }

    public static Specification<ParkingEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";

            return builder.or(
                builder.like(builder.lower(root.get("spotCode")), like),
                builder.like(builder.lower(root.get("vehiclePlate")), like),
                builder.like(builder.lower(root.get("observations")), like),
                builder.like(builder.lower(root.get("unit").get("unitCode")), like),
                builder.like(builder.lower(root.get("unit").get("blockLabel")), like),
                builder.like(builder.lower(root.get("resident").get("firstName")), like),
                builder.like(builder.lower(root.get("resident").get("lastName")), like),
                builder.like(builder.lower(root.get("resident").get("documentNumber")), like)
            );
        };
    }
}
