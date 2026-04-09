package com.domus.server.packages.mapper;

import com.domus.server.packages.dto.response.PackageResponse;
import com.domus.server.packages.dto.response.PackageUserSummaryResponse;
import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class PackageMapper {

    public PackageResponse toResponse(PackageEntity packageEntity) {
        return new PackageResponse(
            packageEntity.getId(),
            packageEntity.getDescription(),
            packageEntity.getSenderName(),
            packageEntity.getPackageType(),
            packageEntity.getResidentName(),
            packageEntity.getUnitLabel(),
            packageEntity.getBlockLabel(),
            packageEntity.getReceivedAt(),
            packageEntity.getDeliveredAt(),
            packageEntity.getStatus(),
            packageEntity.getObservations(),
            packageEntity.getReceivedByName(),
            packageEntity.getDeliveredToName(),
            packageEntity.getCreatedAt(),
            packageEntity.getUpdatedAt(),
            toUserSummary(packageEntity.getResidentUser()),
            toUserSummary(packageEntity.getRecordedByUser())
        );
    }

    private PackageUserSummaryResponse toUserSummary(UserEntity user) {
        if (user == null) {
            return null;
        }

        return new PackageUserSummaryResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
    }
}
