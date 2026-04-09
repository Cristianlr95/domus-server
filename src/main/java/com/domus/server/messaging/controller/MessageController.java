package com.domus.server.messaging.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.messaging.dto.request.SendMessageRequest;
import com.domus.server.messaging.dto.response.MessageResponse;
import com.domus.server.messaging.dto.response.MessagingUserSummaryResponse;
import com.domus.server.messaging.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessagingService messagingService;

    public MessageController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping
    @Operation(summary = "Send a message to another authenticated user")
    @PreAuthorize("hasAuthority('messaging.create')")
    public ApiResponse<MessageResponse> sendMessage(
        @Valid @RequestBody SendMessageRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.sendMessage(authUser.getId(), request));
    }

    @GetMapping
    @Operation(summary = "List messages for a conversation")
    @PreAuthorize("hasAuthority('messaging.read')")
    public ApiResponse<List<MessageResponse>> listMessages(
        @RequestParam UUID conversationId,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.listMessages(authUser.getId(), conversationId));
    }

    @GetMapping("/contacts")
    @Operation(summary = "List available messaging contacts")
    @PreAuthorize("hasAnyAuthority('messaging.read','messaging.create')")
    public ApiResponse<List<MessagingUserSummaryResponse>> listContacts(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.listContacts(authUser.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a received message as read")
    @PreAuthorize("hasAuthority('messaging.read')")
    public ApiResponse<MessageResponse> markAsRead(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.markAsRead(authUser.getId(), id));
    }
}
