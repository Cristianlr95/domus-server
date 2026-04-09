package com.domus.server.packages.dto.request;

import com.domus.server.packages.entity.PackageStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePackageStatusRequest(
    @NotNull
    PackageStatus status
) {
}
