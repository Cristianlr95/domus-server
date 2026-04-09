package com.domus.server.packages.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DeliverPackageRequest(
    @NotBlank
    @Size(max = 150)
    String deliveredToName,

    Instant deliveredAt
) {
}
