package com.domus.server.bookings.dto.response;

import java.util.UUID;

public record BookingUserSummaryResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber
) {
}
