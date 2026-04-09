package com.domus.server.messaging.dto.response;

import com.domus.server.user.entity.RoleName;
import java.util.Set;
import java.util.UUID;

public record MessagingUserSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    Set<RoleName> roles
) {
}
