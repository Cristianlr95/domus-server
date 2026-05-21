package com.domus.server.user.service;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.user.dto.request.CreateUserRequest;
import com.domus.server.user.dto.request.UpdateUserRequest;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.entity.RoleEntity;
import com.domus.server.user.entity.RoleName;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.mapper.UserMapper;
import com.domus.server.user.repository.RoleRepository;
import com.domus.server.user.repository.UserRepository;
import java.util.HashSet;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserCommandService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder,
        AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public UserResponse create(CreateUserRequest request, UUID actorUserId) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("A user with the same email already exists.");
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRoles(new HashSet<>(java.util.List.of(getRole(request.role()))));

        UserResponse response = userMapper.toResponse(userRepository.save(user));
        auditLogService.record(
            actorUserId,
            "USER",
            response.id().toString(),
            AuditAction.CREATE,
            "User " + response.email() + " created.",
            null,
            response,
            java.util.Map.of("role", request.role())
        );
        return response;
    }

    public UserResponse update(UUID id, UpdateUserRequest request, UUID actorUserId) {
        UserEntity user = getUser(id);
        UserResponse previousState = userMapper.toResponse(user);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRoles(new HashSet<>(java.util.List.of(getRole(request.role()))));

        UserResponse response = userMapper.toResponse(userRepository.save(user));
        auditLogService.record(
            actorUserId,
            "USER",
            response.id().toString(),
            AuditAction.UPDATE,
            "User " + response.email() + " updated.",
            previousState,
            response,
            null
        );
        return response;
    }

    public UserResponse updateStatus(UUID id, boolean active, UUID actorUserId) {
        if (id.equals(actorUserId) && !active) {
            throw new IllegalArgumentException("You cannot deactivate your own user.");
        }

        UserEntity user = getUser(id);
        UserResponse previousState = userMapper.toResponse(user);
        boolean previousStatus = user.isActive();
        user.setActive(active);

        UserResponse response = userMapper.toResponse(userRepository.save(user));
        auditLogService.record(
            actorUserId,
            "USER",
            response.id().toString(),
            AuditAction.STATUS_CHANGE,
            "User " + response.email() + " status changed from " + previousStatus + " to " + active + ".",
            previousState,
            response,
            java.util.Map.of("previousActive", previousStatus, "newActive", active)
        );
        return response;
    }

    private UserEntity getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private RoleEntity getRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
