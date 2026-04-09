package com.domus.server.messaging.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.messaging.dto.response.ConversationDetailResponse;
import com.domus.server.messaging.dto.response.ConversationResponse;
import com.domus.server.messaging.service.MessagingService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final MessagingService messagingService;

    public ConversationController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    @Operation(summary = "List current user conversations")
    @PreAuthorize("hasAuthority('messaging.read')")
    public ApiResponse<List<ConversationResponse>> listConversations(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.listConversations(authUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation detail with messages")
    @PreAuthorize("hasAuthority('messaging.read')")
    public ApiResponse<ConversationDetailResponse> getConversation(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(messagingService.getConversationDetail(authUser.getId(), id));
    }
}
