package com.domus.server.residents.dto.response;

import java.util.UUID;

public record ResidentLinkedUserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email
) {
}
