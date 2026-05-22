package com.domus.server.bookings.dto.request;

import com.domus.server.bookings.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBookingStatusRequest(
    @NotNull
    BookingStatus status,

    @Size(max = 500)
    String observations
) {
}
