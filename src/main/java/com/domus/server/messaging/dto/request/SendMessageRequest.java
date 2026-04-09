package com.domus.server.messaging.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendMessageRequest(
    @NotNull UUID recipientUserId,
    @NotBlank @Size(max = 2000) String content
) {
}
