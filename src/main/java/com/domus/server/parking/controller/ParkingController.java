package com.domus.server.parking.controller;

import com.domus.server.common.api.ApiResponse;
import com.domus.server.parking.dto.request.CreateParkingRequest;
import com.domus.server.parking.dto.request.UpdateParkingRequest;
import com.domus.server.parking.dto.request.UpdateParkingStatusRequest;
import com.domus.server.parking.dto.response.ParkingResponse;
import com.domus.server.parking.entity.ParkingOccupancyStatus;
import com.domus.server.parking.entity.ParkingType;
import com.domus.server.parking.service.ParkingService;
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
@RequestMapping("/api/v1/parking")
@PreAuthorize("hasAnyRole('ADMIN','CONSERJERIA')")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping
    @Operation(summary = "Register a new parking spot")
    public ApiResponse<ParkingResponse> create(@Valid @RequestBody CreateParkingRequest request) {
        return ApiResponse.of(parkingService.create(request));
    }

    @GetMapping
    @Operation(summary = "List parking spots with filters")
    public ApiResponse<List<ParkingResponse>> list(
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) ParkingOccupancyStatus occupancyStatus,
        @RequestParam(required = false) ParkingType parkingType,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(parkingService.list(active, occupancyStatus, parkingType, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get parking spot detail")
    public ApiResponse<ParkingResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(parkingService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update parking spot information")
    public ApiResponse<ParkingResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateParkingRequest request
    ) {
        return ApiResponse.of(parkingService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update parking spot active and occupancy status")
    public ApiResponse<ParkingResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateParkingStatusRequest request
    ) {
        return ApiResponse.of(parkingService.updateStatus(id, request));
    }
}
