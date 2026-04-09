package com.domus.server.concierge.dto.response;

import java.time.Instant;
import java.util.List;

public record ConciergeDashboardResponse(
    ConciergeDashboardMetricsResponse metrics,
    List<ConciergeRecentActivityResponse> recentActivity,
    Instant generatedAt
) {
}
