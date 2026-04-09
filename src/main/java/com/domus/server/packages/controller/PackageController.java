package com.domus.server.packages.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.packages.dto.request.CreatePackageRequest;
import com.domus.server.packages.dto.request.DeliverPackageRequest;
import com.domus.server.packages.dto.request.UpdatePackageRequest;
import com.domus.server.packages.dto.request.UpdatePackageStatusRequest;
import com.domus.server.packages.dto.response.PackageResponse;
import com.domus.server.packages.entity.PackageStatus;
import com.domus.server.packages.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/packages")
@PreAuthorize("hasAnyRole('ADMIN','CONSERJERIA')")
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping
    @Operation(summary = "Register a new package")
    public ApiResponse<PackageResponse> create(
        @Valid @RequestBody CreatePackageRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(packageService.create(request, authUser.getId()));
    }

    @GetMapping
    @Operation(summary = "List packages with optional status and search filters")
    public ApiResponse<List<PackageResponse>> list(
        @RequestParam(required = false) PackageStatus status,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(packageService.list(status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get package detail")
    public ApiResponse<PackageResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(packageService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update editable package information")
    public ApiResponse<PackageResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePackageRequest request
    ) {
        return ApiResponse.of(packageService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update package status")
    public ApiResponse<PackageResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePackageStatusRequest request
    ) {
        return ApiResponse.of(packageService.updateStatus(id, request));
    }

    @PatchMapping("/{id}/deliver")
    @Operation(summary = "Mark package as delivered")
    public ApiResponse<PackageResponse> deliver(
        @PathVariable UUID id,
        @Valid @RequestBody DeliverPackageRequest request
    ) {
        return ApiResponse.of(packageService.deliver(id, request));
    }
}
