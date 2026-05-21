package com.domus.server.user.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.user.dto.request.CreateUserRequest;
import com.domus.server.user.dto.request.UpdateUserRequest;
import com.domus.server.user.dto.response.PermissionResponse;
import com.domus.server.user.dto.response.RoleResponse;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.entity.RoleName;
import com.domus.server.user.service.UserCommandService;
import com.domus.server.user.service.UserQueryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UserController(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.of(userQueryService.getUserById(authUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users.read')")
    public ApiResponse<List<UserResponse>> listUsers(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) RoleName role,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(userQueryService.listUsers(active, role, search));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserResponse> create(
        @Valid @RequestBody CreateUserRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(userCommandService.create(request, authUser.getId()));
    }

    @GetMapping("/roles/catalog")
    @PreAuthorize("hasAuthority('roles.read')")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.of(userQueryService.listRoles());
    }

    @GetMapping("/permissions/catalog")
    @PreAuthorize("hasAuthority('permissions.read')")
    public ApiResponse<List<PermissionResponse>> listPermissions() {
        return ApiResponse.of(userQueryService.listPermissions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users.read')")
    public ApiResponse<UserResponse> getUser(@PathVariable UUID id) {
        return ApiResponse.of(userQueryService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(userCommandService.update(id, request, authUser.getId()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserResponse> activate(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(userCommandService.updateStatus(id, true, authUser.getId()));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('users.manage')")
    public ApiResponse<UserResponse> deactivate(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(userCommandService.updateStatus(id, false, authUser.getId()));
    }
}
