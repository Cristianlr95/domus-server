package com.domus.server.packages.dto.response;

import com.domus.server.packages.entity.PackageStatus;
import com.domus.server.packages.entity.PackageType;
import java.time.Instant;
import java.util.UUID;

public record PackageResponse(
    UUID id,
    String description,
    String senderName,
    PackageType packageType,
    String residentName,
    String unitLabel,
    String blockLabel,
    Instant receivedAt,
    Instant deliveredAt,
    PackageStatus status,
    String observations,
    String receivedByName,
    String deliveredToName,
    Instant createdAt,
    Instant updatedAt,
    PackageUserSummaryResponse residentUser,
    PackageUserSummaryResponse recordedByUser
) {
}
