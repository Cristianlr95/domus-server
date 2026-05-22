package com.domus.server.admin.controller;

import com.domus.server.admin.dto.response.AdminDashboardResponse;
import com.domus.server.admin.dto.response.AdminRecentActivityResponse;
import com.domus.server.admin.service.AdminDashboardService;
import com.domus.server.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get administrative dashboard")
    @PreAuthorize("hasAuthority('admin.dashboard.read')")
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.of(adminDashboardService.getDashboard());
    }

    @GetMapping("/recent-activity")
    @Operation(summary = "Get administrative recent audit activity")
    @PreAuthorize("hasAuthority('admin.dashboard.read')")
    public ApiResponse<List<AdminRecentActivityResponse>> getRecentActivity(
        @RequestParam(defaultValue = "8") int limit
    ) {
        return ApiResponse.of(adminDashboardService.getRecentActivity(limit));
    }
}
