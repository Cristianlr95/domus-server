package com.domus.server.bookings.controller;

import com.domus.server.bookings.dto.request.CreateBookingRequest;
import com.domus.server.bookings.dto.request.UpdateBookingStatusRequest;
import com.domus.server.bookings.dto.response.BookingResponse;
import com.domus.server.bookings.dto.response.CommonSpaceResponse;
import com.domus.server.bookings.entity.BookingStatus;
import com.domus.server.bookings.entity.SpaceType;
import com.domus.server.bookings.service.BookingService;
import com.domus.server.common.api.ApiResponse;
import com.domus.server.common.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/spaces")
    @Operation(summary = "List active common spaces")
    @PreAuthorize("hasAuthority('bookings.read') or hasAuthority('bookings.create')")
    public ApiResponse<List<CommonSpaceResponse>> listSpaces() {
        return ApiResponse.of(bookingService.listSpaces());
    }

    @PostMapping
    @Operation(summary = "Create a common space booking")
    @PreAuthorize("hasAuthority('bookings.create')")
    public ApiResponse<BookingResponse> create(
        @Valid @RequestBody CreateBookingRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(bookingService.create(request, authUser.getId()));
    }

    @GetMapping
    @Operation(summary = "List bookings with filters")
    @PreAuthorize("hasAuthority('bookings.read')")
    public ApiResponse<List<BookingResponse>> list(
        @RequestParam(required = false) BookingStatus status,
        @RequestParam(required = false) SpaceType spaceType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(bookingService.list(status, spaceType, startDate, endDate, search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking detail")
    @PreAuthorize("hasAuthority('bookings.read')")
    public ApiResponse<BookingResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(bookingService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update booking status")
    @PreAuthorize("hasAuthority('bookings.update')")
    public ApiResponse<BookingResponse> updateStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateBookingStatusRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(bookingService.updateStatus(id, request, authUser.getId()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking")
    @PreAuthorize("hasAuthority('bookings.update') or hasAuthority('bookings.create')")
    public ApiResponse<BookingResponse> cancel(
        @PathVariable UUID id,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.of(bookingService.cancel(id, authUser.getId()));
    }
}
