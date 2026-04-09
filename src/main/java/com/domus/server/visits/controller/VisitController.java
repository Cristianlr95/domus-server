package com.domus.server.visits.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.visits.dto.request.CreateVisitRequest;
import com.domus.server.visits.dto.request.UpdateVisitRequest;
import com.domus.server.visits.dto.request.UpdateVisitStatusRequest;
import com.domus.server.visits.dto.response.VisitResponse;
import com.domus.server.visits.entity.VisitStatus;
import com.domus.server.visits.service.VisitService;
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
@RequestMapping("/api/v1/visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @PostMapping
    @Operation(summary = "Register a new visit")
    @PreAuthorize("hasAuthority('visits.create')")
    public ApiResponse<VisitResponse> create(
        @Valid @RequestBody CreateVisitRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(visitService.create(request, authUser.getId()));
    }

    @GetMapping
    @Operation(summary = "List visits with optional status and search filters")
    @PreAuthorize("hasAuthority('visits.read')")
    public ApiResponse<List<VisitResponse>> list(
        @RequestParam(required = false) VisitStatus status,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(visitService.list(status, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get visit detail")
    @PreAuthorize("hasAuthority('visits.read')")
    public ApiResponse<VisitResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(visitService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update editable visit information")
    @PreAuthorize("hasAuthority('visits.update')")
    public ApiResponse<VisitResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateVisitRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(visitService.update(id, request, authUser.getId()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update visit status")
    @PreAuthorize("hasAuthority('visits.update')")
    public ApiResponse<VisitResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateVisitStatusRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(visitService.updateStatus(id, request, authUser.getId()));
    }
}
