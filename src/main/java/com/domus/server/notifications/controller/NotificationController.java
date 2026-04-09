package com.domus.server.notifications.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.notifications.dto.response.NotificationResponse;
import com.domus.server.notifications.dto.response.NotificationUnreadCountResponse;
import com.domus.server.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List current user notifications")
    public ApiResponse<List<NotificationResponse>> listNotifications(
        @AuthenticationPrincipal AuthUser authUser,
        @RequestParam(required = false) Boolean unreadOnly
    ) {
        return ApiResponse.of(notificationService.listForUser(authUser.getId(), unreadOnly));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for current user")
    public ApiResponse<NotificationUnreadCountResponse> unreadCount(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(notificationService.getUnreadCount(authUser.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ApiResponse<NotificationResponse> markAsRead(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(notificationService.markAsRead(authUser.getId(), id));
    }
}
