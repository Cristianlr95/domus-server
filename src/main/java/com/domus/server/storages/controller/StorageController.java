package com.domus.server.storages.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.storages.dto.request.CreateStorageRequest;
import com.domus.server.storages.dto.request.UpdateStorageRequest;
import com.domus.server.storages.dto.request.UpdateStorageStatusRequest;
import com.domus.server.storages.dto.response.StorageResponse;
import com.domus.server.storages.entity.StorageOccupancyStatus;
import com.domus.server.storages.service.StorageService;
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
@RequestMapping("/api/v1/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    @Operation(summary = "Register a new storage")
    @PreAuthorize("hasAuthority('storages.manage')")
    public ApiResponse<StorageResponse> create(
        @Valid @RequestBody CreateStorageRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(storageService.create(request, authUser.getId()));
    }

    @GetMapping
    @Operation(summary = "List storages with filters")
    @PreAuthorize("hasAuthority('storages.read')")
    public ApiResponse<List<StorageResponse>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) StorageOccupancyStatus occupancyStatus,
        @RequestParam(required = false) UUID unitId,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(storageService.list(active, occupancyStatus, unitId, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get storage detail")
    @PreAuthorize("hasAuthority('storages.read')")
    public ApiResponse<StorageResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(storageService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update storage information")
    @PreAuthorize("hasAuthority('storages.manage')")
    public ApiResponse<StorageResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStorageRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(storageService.update(id, request, authUser.getId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update storage active and occupancy status")
    @PreAuthorize("hasAuthority('storages.manage')")
    public ApiResponse<StorageResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStorageStatusRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(storageService.updateStatus(id, request, authUser.getId()));
    }
}
