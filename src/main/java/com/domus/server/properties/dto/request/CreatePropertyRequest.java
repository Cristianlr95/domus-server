package com.domus.server.properties.dto.request;

import com.domus.server.properties.entity.PropertyStatus;
import com.domus.server.properties.entity.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreatePropertyRequest(
    @NotBlank
    @Size(max = 80)
    String label,

    @Size(max = 80)
    String blockLabel,

    @NotNull
    PropertyType type,

    @NotNull
    PropertyStatus status,

    @Min(0)
    @Max(20)
    int bedrooms,

    @Min(0)
    @Max(20)
    int bathrooms,

    @DecimalMin("0.0")
    BigDecimal squareMeters,

    @Min(-5)
    @Max(200)
    Integer floor,

    @NotBlank
    @Size(max = 150)
    String ownerName,

    @NotBlank
    @Email
    @Size(max = 150)
    String ownerEmail,

    @Size(max = 50)
    String ownerPhone,

    @Min(0)
    @Max(100)
    Integer residentsCount,

    @Size(max = 500)
    String observations,

    @Size(max = 500)
    String imageUrl
) {
}
