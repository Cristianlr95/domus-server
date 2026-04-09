package com.domus.server.concierge.dto.response;

public record ConciergeDashboardMetricsResponse(
    long activeVisits,
    long pendingVisits,
    long pendingPackages,
    long activeResidents,
    long activeUnits
) {
}
