package com.domus.server.visits.mapper;

import com.domus.server.user.entity.UserEntity;
import com.domus.server.visits.dto.response.VisitResponse;
import com.domus.server.visits.dto.response.VisitUserSummaryResponse;
import com.domus.server.visits.entity.VisitEntity;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {

    public VisitResponse toResponse(VisitEntity visit) {
        return new VisitResponse(
            visit.getId(),
            visit.getVisitorName(),
            visit.getVisitorDocument(),
            visit.getVisitorPhone(),
            visit.getVehiclePlate(),
            visit.getResidentName(),
            visit.getUnitLabel(),
            visit.getBlockLabel(),
            visit.getStatus(),
            visit.getRegistrationType(),
            visit.getObservations(),
            visit.getEntryAt(),
            visit.getExitAt(),
            visit.getCreatedAt(),
            visit.getUpdatedAt(),
            toUserSummary(visit.getResidentUser()),
            toUserSummary(visit.getRecordedByUser())
        );
    }

    private VisitUserSummaryResponse toUserSummary(UserEntity user) {
        if (user == null) {
            return null;
        }

        return new VisitUserSummaryResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
    }
}
