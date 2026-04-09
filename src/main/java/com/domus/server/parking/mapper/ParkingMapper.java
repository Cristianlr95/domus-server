package com.domus.server.parking.mapper;

import com.domus.server.parking.dto.response.ParkingResidentSummaryResponse;
import com.domus.server.parking.dto.response.ParkingResponse;
import com.domus.server.parking.dto.response.ParkingUnitSummaryResponse;
import com.domus.server.parking.entity.ParkingEntity;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.units.entity.UnitEntity;
import org.springframework.stereotype.Component;

@Component
public class ParkingMapper {

    public ParkingResponse toResponse(ParkingEntity parking) {
        return new ParkingResponse(
            parking.getId(),
            parking.getSpotCode(),
            parking.getParkingType(),
            parking.getOccupancyStatus(),
            parking.isActive(),
            parking.getVehiclePlate(),
            parking.getObservations(),
            parking.getCreatedAt(),
            parking.getUpdatedAt(),
            toUnitSummary(parking.getUnit()),
            toResidentSummary(parking.getResident())
        );
    }

    private ParkingUnitSummaryResponse toUnitSummary(UnitEntity unit) {
        if (unit == null) {
            return null;
        }

        return new ParkingUnitSummaryResponse(
            unit.getId(),
            unit.getUnitCode(),
            unit.getBlockLabel(),
            unit.getFloorNumber()
        );
    }

    private ParkingResidentSummaryResponse toResidentSummary(ResidentEntity resident) {
        if (resident == null) {
            return null;
        }

        return new ParkingResidentSummaryResponse(
            resident.getId(),
            resident.getFirstName(),
            resident.getLastName(),
            resident.getDocumentNumber(),
            resident.getResidentType(),
            resident.isActive()
        );
    }
}
