package com.domus.server.visits.dto.request;

import com.domus.server.visits.entity.VisitRegistrationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateVisitRequest(
    @NotBlank @Size(max = 150) String visitorName,
    @NotBlank @Size(max = 50) String visitorDocument,
    @Size(max = 50) String visitorPhone,
    @Size(max = 20) String vehiclePlate,
    UUID residentUserId,
    @NotBlank @Size(max = 150) String residentName,
    @Size(max = 80) String unitLabel,
    @Size(max = 80) String blockLabel,
    @Size(max = 500) String observations,
    VisitRegistrationType registrationType
) {
}
