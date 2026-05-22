package com.domus.server.properties.dto.response;

import com.domus.server.properties.entity.PropertyStatus;
import com.domus.server.properties.entity.PropertyType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PropertyResponse(
    UUID id,
    String label,
    String blockLabel,
    PropertyType type,
    PropertyStatus status,
    int bedrooms,
    int bathrooms,
    BigDecimal squareMeters,
    Integer floor,
    String ownerName,
    String ownerEmail,
    String ownerPhone,
    Integer residentsCount,
    String observations,
    String imageUrl,
    Instant createdAt,
    Instant updatedAt
) {
}
