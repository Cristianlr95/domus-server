package com.domus.server.bookings.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull
    UUID commonSpaceId,

    @NotNull
    @FutureOrPresent
    LocalDate bookingDate,

    @NotNull
    LocalTime startTime,

    @NotNull
    LocalTime endTime,

    @Min(1)
    @Max(500)
    Integer guestCount,

    @Size(max = 500)
    String observations
) {
}
