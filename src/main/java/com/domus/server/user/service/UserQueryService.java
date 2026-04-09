package com.domus.server.user.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.user.dto.response.PermissionResponse;
import com.domus.server.user.dto.response.RoleResponse;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.mapper.UserMapper;
import com.domus.server.user.repository.PermissionRepository;
import com.domus.server.user.repository.RoleRepository;
import com.domus.server.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;

    public UserQueryService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PermissionRepository permissionRepository,
        UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(userMapper::toResponse)
            .toList();
    }

    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
            .map(userMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
            .map(userMapper::toResponse)
            .toList();
    }

    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
            .map(userMapper::toResponse)
            .toList();
    }
}
