package com.domus.server.visits.dto.response;

import java.util.UUID;

public record VisitUserSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String email
) {
}
