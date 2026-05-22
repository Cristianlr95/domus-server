package com.domus.server.admin.dto.response;

import java.time.Instant;
import java.util.List;

public record AdminDashboardResponse(
    AdminDashboardMetricsResponse metrics,
    List<AdminRecentActivityResponse> recentActivity,
    Instant generatedAt
) {
}
