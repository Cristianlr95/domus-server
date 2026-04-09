package com.domus.server.user.dto.response;

import java.util.UUID;

public record PermissionResponse(UUID id, String code, String description) {
}
