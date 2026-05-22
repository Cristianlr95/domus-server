package com.domus.server.admin.dto.response;

public record AdminDashboardMetricsResponse(
    long totalUsers,
    long activeUsers,
    long activeResidents,
    long activeUnits,
    long activeParkingSpots,
    long activeStorages,
    long pendingVisits,
    long pendingPackages,
    long unreadNotifications
) {
}
