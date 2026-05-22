package com.domus.server.bookings.dto.response;

import com.domus.server.bookings.entity.BookingStatus;
import com.domus.server.bookings.entity.SpaceType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID commonSpaceId,
    String commonSpaceName,
    SpaceType commonSpaceType,
    UUID residentId,
    String residentName,
    String unitLabel,
    String blockLabel,
    BookingStatus status,
    LocalDate bookingDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer guestCount,
    String observations,
    Instant createdAt,
    Instant updatedAt,
    BookingUserSummaryResponse residentUser,
    BookingUserSummaryResponse approvedByUser
) {
}
