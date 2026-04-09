package com.domus.server.residents.dto.request;

import com.domus.server.residents.entity.ResidentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateResidentRequest(
    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @NotBlank
    @Size(max = 50)
    String documentNumber,

    @Email
    @Size(max = 150)
    String email,

    @Size(max = 50)
    String phone,

    @NotNull
    ResidentType residentType,

    UUID linkedUserId,

    UUID unitId
) {
}
