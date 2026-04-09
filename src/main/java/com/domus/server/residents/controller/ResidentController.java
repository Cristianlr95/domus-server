package com.domus.server.residents.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.residents.dto.request.CreateResidentRequest;
import com.domus.server.residents.dto.request.UpdateResidentRequest;
import com.domus.server.residents.dto.request.UpdateResidentStatusRequest;
import com.domus.server.residents.dto.response.ResidentLinkedUserResponse;
import com.domus.server.residents.dto.response.ResidentResponse;
import com.domus.server.residents.service.ResidentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @PostMapping
    @Operation(summary = "Register a new resident")
    @PreAuthorize("hasAuthority('residents.manage')")
    public ApiResponse<ResidentResponse> create(@Valid @RequestBody CreateResidentRequest request) {
        return ApiResponse.of(residentService.create(request));
    }

    @GetMapping
    @Operation(summary = "List residents with optional active and search filters")
    @PreAuthorize("hasAuthority('residents.read')")
    public ApiResponse<List<ResidentResponse>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(residentService.list(active, search));
    }

    @GetMapping("/linkable-users")
    @Operation(summary = "List resident users available to link")
    @PreAuthorize("hasAuthority('residents.manage')")
    public ApiResponse<List<ResidentLinkedUserResponse>> listLinkableUsers(
        @RequestParam(required = false) UUID currentUserId
    ) {
        return ApiResponse.of(residentService.listLinkableUsers(currentUserId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resident detail")
    @PreAuthorize("hasAuthority('residents.read')")
    public ApiResponse<ResidentResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(residentService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update resident information")
    @PreAuthorize("hasAuthority('residents.manage')")
    public ApiResponse<ResidentResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateResidentRequest request
    ) {
        return ApiResponse.of(residentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update resident active status")
    @PreAuthorize("hasAuthority('residents.manage')")
    public ApiResponse<ResidentResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateResidentStatusRequest request
    ) {
        return ApiResponse.of(residentService.updateStatus(id, request));
    }
}
