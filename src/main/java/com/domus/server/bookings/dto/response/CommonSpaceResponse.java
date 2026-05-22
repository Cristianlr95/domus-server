package com.domus.server.bookings.dto.response;

import com.domus.server.bookings.entity.SpaceType;
import java.util.UUID;

public record CommonSpaceResponse(
    UUID id,
    String name,
    SpaceType type,
    int capacity,
    String description,
    String imageUrl,
    boolean isActive
) {
}
