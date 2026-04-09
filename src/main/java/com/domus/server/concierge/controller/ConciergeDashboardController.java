package com.domus.server.concierge.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.concierge.dto.response.ConciergeDashboardResponse;
import com.domus.server.concierge.dto.response.ConciergeRecentActivityResponse;
import com.domus.server.concierge.service.ConciergeDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/concierge")
public class ConciergeDashboardController {

    private final ConciergeDashboardService conciergeDashboardService;

    public ConciergeDashboardController(ConciergeDashboardService conciergeDashboardService) {
        this.conciergeDashboardService = conciergeDashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get concierge operational dashboard")
    @PreAuthorize("hasAuthority('concierge.dashboard.read')")
    public ApiResponse<ConciergeDashboardResponse> getDashboard() {
        return ApiResponse.of(conciergeDashboardService.getDashboard());
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get concierge recent activity")
    @PreAuthorize("hasAuthority('concierge.dashboard.read')")
    public ApiResponse<List<ConciergeRecentActivityResponse>> getRecentActivity(
        @RequestParam(defaultValue = "8") int limit
    ) {
        return ApiResponse.of(conciergeDashboardService.getRecentActivity(limit));
    }
}
