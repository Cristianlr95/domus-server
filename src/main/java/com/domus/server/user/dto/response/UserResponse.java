package com.domus.server.user.dto.response;

import com.domus.server.user.entity.RoleName;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    boolean active,
    Set<RoleName> roles,
    Instant createdAt,
    Instant updatedAt
) {
}
