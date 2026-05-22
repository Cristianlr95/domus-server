package com.domus.server.properties.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import com.domus.server.properties.dto.request.CreatePropertyRequest;
import com.domus.server.properties.dto.request.UpdatePropertyRequest;
import com.domus.server.properties.dto.response.PropertyResponse;
import com.domus.server.properties.entity.PropertyStatus;
import com.domus.server.properties.entity.PropertyType;
import com.domus.server.properties.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    @Operation(summary = "Register a new property")
    @PreAuthorize("hasAuthority('properties.manage')")
    public ApiResponse<PropertyResponse> create(
        @Valid @RequestBody CreatePropertyRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(propertyService.create(request, authUser.getId()));
    }

    @GetMapping
    @Operation(summary = "List properties with filters")
    @PreAuthorize("hasAuthority('properties.read')")
    public ApiResponse<List<PropertyResponse>> list(
        @RequestParam(required = false) PropertyType type,
        @RequestParam(required = false) PropertyStatus status,
        @RequestParam(required = false) String block,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(propertyService.list(type, status, block, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get property detail")
    @PreAuthorize("hasAuthority('properties.read')")
    public ApiResponse<PropertyResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(propertyService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update property information")
    @PreAuthorize("hasAuthority('properties.manage')")
    public ApiResponse<PropertyResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePropertyRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(propertyService.update(id, request, authUser.getId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete property")
    @PreAuthorize("hasAuthority('properties.manage')")
    public void delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        propertyService.delete(id, authUser.getId());
    }
}
