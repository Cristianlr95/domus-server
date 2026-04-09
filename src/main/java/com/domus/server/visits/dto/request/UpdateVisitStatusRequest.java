package com.domus.server.visits.dto.request;

import com.domus.server.visits.entity.VisitStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVisitStatusRequest(@NotNull VisitStatus status) {
}
