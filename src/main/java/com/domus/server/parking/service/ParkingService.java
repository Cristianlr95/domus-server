package com.domus.server.parking.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.parking.dto.request.CreateParkingRequest;
import com.domus.server.parking.dto.request.UpdateParkingRequest;
import com.domus.server.parking.dto.request.UpdateParkingStatusRequest;
import com.domus.server.parking.dto.response.ParkingResponse;
import com.domus.server.parking.entity.ParkingEntity;
import com.domus.server.parking.entity.ParkingOccupancyStatus;
import com.domus.server.parking.entity.ParkingType;
import com.domus.server.parking.mapper.ParkingMapper;
import com.domus.server.parking.repository.ParkingRepository;
import com.domus.server.parking.support.ParkingSpecifications;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.units.entity.UnitEntity;
import com.domus.server.units.repository.UnitRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final ParkingMapper parkingMapper;

    public ParkingService(
        ParkingRepository parkingRepository,
        UnitRepository unitRepository,
        ResidentRepository residentRepository,
        ParkingMapper parkingMapper
    ) {
        this.parkingRepository = parkingRepository;
        this.unitRepository = unitRepository;
        this.residentRepository = residentRepository;
        this.parkingMapper = parkingMapper;
    }

    public ParkingResponse create(CreateParkingRequest request) {
        validateUniqueSpotCode(request.spotCode(), null);

        ParkingEntity parking = new ParkingEntity();
        parking.setId(UUID.randomUUID());
        parking.setActive(true);

        applyEditableFields(
            parking,
            request.spotCode(),
            request.parkingType(),
            request.occupancyStatus(),
            request.unitId(),
            request.residentId(),
            request.vehiclePlate(),
            request.observations()
        );

        return parkingMapper.toResponse(parkingRepository.save(parking));
    }

    @Transactional(readOnly = true)
    public List<ParkingResponse> list(Boolean active, ParkingOccupancyStatus occupancyStatus, ParkingType parkingType, String search) {
        Specification<ParkingEntity> specification = ParkingSpecifications.withActive(active)
            .and(ParkingSpecifications.withOccupancyStatus(occupancyStatus))
            .and(ParkingSpecifications.withType(parkingType))
            .and(ParkingSpecifications.search(search));

        return parkingRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "spotCode"))
            .stream()
            .map(parkingMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ParkingResponse getById(UUID id) {
        return parkingMapper.toResponse(getParking(id));
    }

    public ParkingResponse update(UUID id, UpdateParkingRequest request) {
        ParkingEntity parking = getParking(id);
        validateUniqueSpotCode(request.spotCode(), parking.getId());

        applyEditableFields(
            parking,
            request.spotCode(),
            request.parkingType(),
            request.occupancyStatus(),
            request.unitId(),
            request.residentId(),
            request.vehiclePlate(),
            request.observations()
        );

        return parkingMapper.toResponse(parkingRepository.save(parking));
    }

    public ParkingResponse updateStatus(UUID id, UpdateParkingStatusRequest request) {
        ParkingEntity parking = getParking(id);

        if (!request.active() && request.occupancyStatus() == ParkingOccupancyStatus.OCUPADO) {
            throw new IllegalArgumentException("An inactive parking spot cannot be marked as occupied.");
        }

        if (
            parking.getParkingType() == ParkingType.VISITA
            && request.occupancyStatus() == ParkingOccupancyStatus.OCUPADO
            && blankToNull(parking.getVehiclePlate()) == null
        ) {
            throw new IllegalArgumentException("A visitor parking spot marked as occupied should include a vehicle plate.");
        }

        parking.setActive(request.active());
        parking.setOccupancyStatus(request.occupancyStatus());
        return parkingMapper.toResponse(parkingRepository.save(parking));
    }

    private ParkingEntity getParking(UUID id) {
        return parkingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parking spot not found."));
    }

    private void validateUniqueSpotCode(String spotCode, UUID currentId) {
        String normalizedSpotCode = spotCode.trim();
        boolean exists = currentId == null
            ? parkingRepository.existsBySpotCodeIgnoreCase(normalizedSpotCode)
            : parkingRepository.existsBySpotCodeIgnoreCaseAndIdNot(normalizedSpotCode, currentId);

        if (exists) {
            throw new IllegalArgumentException("A parking spot with the same code already exists.");
        }
    }

    private void applyEditableFields(
        ParkingEntity parking,
        String spotCode,
        ParkingType parkingType,
        ParkingOccupancyStatus occupancyStatus,
        UUID unitId,
        UUID residentId,
        String vehiclePlate,
        String observations
    ) {
        UnitEntity unit = resolveUnit(unitId);
        ResidentEntity resident = resolveResident(residentId);

        if (resident != null && resident.getUnit() != null) {
            if (unit == null) {
                unit = resident.getUnit();
            } else if (!resident.getUnit().getId().equals(unit.getId())) {
                throw new IllegalArgumentException("The selected resident is associated with a different unit.");
            }
        }

        if (parkingType == ParkingType.RESIDENTE && resident == null && unit == null) {
            throw new IllegalArgumentException("A resident parking spot must be associated with a resident or unit.");
        }

        if (occupancyStatus == ParkingOccupancyStatus.OCUPADO && blankToNull(vehiclePlate) == null && parkingType == ParkingType.VISITA) {
            throw new IllegalArgumentException("A visitor parking spot marked as occupied should include a vehicle plate.");
        }

        parking.setSpotCode(spotCode.trim());
        parking.setParkingType(parkingType);
        parking.setOccupancyStatus(occupancyStatus);
        parking.setUnit(unit);
        parking.setResident(resident);
        parking.setVehiclePlate(normalizePlate(vehiclePlate));
        parking.setObservations(blankToNull(observations));

        if (!parking.isActive() && occupancyStatus == ParkingOccupancyStatus.OCUPADO) {
            throw new IllegalArgumentException("An inactive parking spot cannot be marked as occupied.");
        }
    }

    private UnitEntity resolveUnit(UUID unitId) {
        if (unitId == null) {
            return null;
        }

        return unitRepository.findById(unitId)
            .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));
    }

    private ResidentEntity resolveResident(UUID residentId) {
        if (residentId == null) {
            return null;
        }

        return residentRepository.findById(residentId)
            .orElseThrow(() -> new ResourceNotFoundException("Resident not found."));
    }

    private String normalizePlate(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
