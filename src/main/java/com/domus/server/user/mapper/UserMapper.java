package com.domus.server.user.mapper;

import com.domus.server.user.dto.response.RoleResponse;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.dto.response.PermissionResponse;
import com.domus.server.user.entity.PermissionEntity;
import com.domus.server.user.entity.RoleEntity;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.isActive(),
            user.getRoleNames(),
            user.getPermissionCodes(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public RoleResponse toResponse(RoleEntity role) {
        return new RoleResponse(
            role.getId(),
            role.getName(),
            role.getPermissions().stream().map(PermissionEntity::getCode).collect(java.util.stream.Collectors.toSet())
        );
    }

    public PermissionResponse toResponse(PermissionEntity permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getDescription());
    }
}
