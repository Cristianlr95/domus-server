package com.domus.server.units.mapper;

import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.units.dto.response.UnitResidentSummaryResponse;
import com.domus.server.units.dto.response.UnitResponse;
import com.domus.server.units.entity.UnitEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {

    public UnitResponse toResponse(UnitEntity unit, List<ResidentEntity> residents) {
        return new UnitResponse(
            unit.getId(),
            unit.getUnitCode(),
            unit.getBlockLabel(),
            unit.getFloorNumber(),
            unit.isActive(),
            unit.getObservations(),
            unit.getCreatedAt(),
            unit.getUpdatedAt(),
            residents.stream().map(this::toResidentSummary).toList()
        );
    }

    private UnitResidentSummaryResponse toResidentSummary(ResidentEntity resident) {
        return new UnitResidentSummaryResponse(
            resident.getId(),
            resident.getFirstName(),
            resident.getLastName(),
            resident.getDocumentNumber(),
            resident.isActive(),
            resident.getResidentType()
        );
    }
}
