package com.domus.server.packages.dto.request;

import com.domus.server.packages.entity.PackageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdatePackageRequest(
    @NotBlank
    @Size(max = 180)
    String description,

    @Size(max = 150)
    String senderName,

    @NotNull
    PackageType packageType,

    UUID residentUserId,

    @Size(max = 150)
    String residentName,

    @Size(max = 80)
    String unitLabel,

    @Size(max = 80)
    String blockLabel,

    Instant receivedAt,

    @Size(max = 500)
    String observations,

    @Size(max = 150)
    String receivedByName
) {
}
