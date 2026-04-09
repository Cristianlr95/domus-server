package com.domus.server.user.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.user.dto.response.PermissionResponse;
import com.domus.server.user.dto.response.RoleResponse;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.service.UserQueryService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.of(userQueryService.getUserById(authUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users.read')")
    public ApiResponse<List<UserResponse>> listUsers() {
        return ApiResponse.of(userQueryService.listUsers());
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
}
