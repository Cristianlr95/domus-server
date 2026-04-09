package com.domus.server.packages.dto.response;

import java.util.UUID;

public record PackageUserSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String email
) {
}
