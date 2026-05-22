package com.domus.server.properties.dto.request;

import com.domus.server.properties.entity.PropertyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdatePropertyRequest(
    PropertyStatus status,

    @Size(max = 150)
    String ownerName,

    @Email
    @Size(max = 150)
    String ownerEmail,

    @Size(max = 50)
    String ownerPhone,

    @Min(0)
    @Max(100)
    Integer residentsCount,

    @Size(max = 500)
    String observations
) {
}
