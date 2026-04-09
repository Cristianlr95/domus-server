package com.domus.server.user.dto.response;

import com.domus.server.user.entity.RoleName;
import java.util.UUID;

public record RoleResponse(UUID id, RoleName name) {
}
