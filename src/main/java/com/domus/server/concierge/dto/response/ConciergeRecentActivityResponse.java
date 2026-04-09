package com.domus.server.concierge.dto.response;

import java.time.Instant;

public record ConciergeRecentActivityResponse(
    String type,
    String title,
    String subtitle,
    String status,
    Instant occurredAt,
    String route
) {
}
