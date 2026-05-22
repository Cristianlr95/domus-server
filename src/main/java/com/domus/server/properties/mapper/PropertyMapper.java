package com.domus.server.properties.mapper;

import com.domus.server.properties.dto.response.PropertyResponse;
import com.domus.server.properties.entity.PropertyEntity;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    public PropertyResponse toResponse(PropertyEntity property) {
        return new PropertyResponse(
            property.getId(),
            property.getLabel(),
            property.getBlockLabel(),
            property.getType(),
            property.getStatus(),
            property.getBedrooms(),
            property.getBathrooms(),
            property.getSquareMeters(),
            property.getFloor(),
            property.getOwnerName(),
            property.getOwnerEmail(),
            property.getOwnerPhone(),
            property.getResidentsCount(),
            property.getObservations(),
            property.getImageUrl(),
            property.getCreatedAt(),
            property.getUpdatedAt()
        );
    }
}
