package com.domus.server.units.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.units.dto.request.CreateUnitRequest;
import com.domus.server.units.dto.request.UpdateUnitRequest;
import com.domus.server.units.dto.request.UpdateUnitStatusRequest;
import com.domus.server.units.dto.response.UnitResponse;
import com.domus.server.units.service.UnitService;
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
@RequestMapping("/api/v1/units")
@PreAuthorize("hasAnyRole('ADMIN','CONSERJERIA')")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @PostMapping
    @Operation(summary = "Register a new unit")
    public ApiResponse<UnitResponse> create(@Valid @RequestBody CreateUnitRequest request) {
        return ApiResponse.of(unitService.create(request));
    }

    @GetMapping
    @Operation(summary = "List units with optional active and search filters")
    public ApiResponse<List<UnitResponse>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(unitService.list(active, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get unit detail")
    public ApiResponse<UnitResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(unitService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update unit information")
    public ApiResponse<UnitResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUnitRequest request
    ) {
        return ApiResponse.of(unitService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update unit active status")
    public ApiResponse<UnitResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUnitStatusRequest request
    ) {
        return ApiResponse.of(unitService.updateStatus(id, request));
    }
}
