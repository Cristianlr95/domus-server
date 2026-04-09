package com.domus.server.units.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.units.dto.request.CreateUnitRequest;
import com.domus.server.units.dto.request.UpdateUnitRequest;
import com.domus.server.units.dto.request.UpdateUnitStatusRequest;
import com.domus.server.units.dto.response.UnitResponse;
import com.domus.server.units.entity.UnitEntity;
import com.domus.server.units.mapper.UnitMapper;
import com.domus.server.units.repository.UnitRepository;
import com.domus.server.units.support.UnitSpecifications;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnitService {

    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final UnitMapper unitMapper;

    public UnitService(UnitRepository unitRepository, ResidentRepository residentRepository, UnitMapper unitMapper) {
        this.unitRepository = unitRepository;
        this.residentRepository = residentRepository;
        this.unitMapper = unitMapper;
    }

    public UnitResponse create(CreateUnitRequest request) {
        validateUniqueUnit(request.unitCode(), request.blockLabel(), null);

        UnitEntity unit = new UnitEntity();
        unit.setId(UUID.randomUUID());
        unit.setActive(true);
        applyEditableFields(unit, request.unitCode(), request.blockLabel(), request.floorNumber(), request.observations());

        UnitEntity savedUnit = unitRepository.save(unit);
        assignResidents(savedUnit, request.residentIds());
        return toResponse(savedUnit);
    }

    @Transactional(readOnly = true)
    public List<UnitResponse> list(Boolean active, String search) {
        Specification<UnitEntity> specification = UnitSpecifications.withActive(active)
            .and(UnitSpecifications.search(search));

        return unitRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "blockLabel", "unitCode"))
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UnitResponse getById(UUID id) {
        return toResponse(getUnit(id));
    }

    public UnitResponse update(UUID id, UpdateUnitRequest request) {
        UnitEntity unit = getUnit(id);
        validateUniqueUnit(request.unitCode(), request.blockLabel(), unit.getId());

        applyEditableFields(unit, request.unitCode(), request.blockLabel(), request.floorNumber(), request.observations());
        UnitEntity savedUnit = unitRepository.save(unit);
        assignResidents(savedUnit, request.residentIds());
        return toResponse(savedUnit);
    }

    public UnitResponse updateStatus(UUID id, UpdateUnitStatusRequest request) {
        UnitEntity unit = getUnit(id);
        unit.setActive(request.active());
        return toResponse(unitRepository.save(unit));
    }

    private UnitEntity getUnit(UUID id) {
        return unitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));
    }

    private void validateUniqueUnit(String unitCode, String blockLabel, UUID currentUnitId) {
        String normalizedUnitCode = unitCode.trim();
        String normalizedBlockLabel = blockLabel.trim();

        boolean exists = currentUnitId == null
            ? unitRepository.existsByUnitCodeIgnoreCaseAndBlockLabelIgnoreCase(normalizedUnitCode, normalizedBlockLabel)
            : unitRepository.existsByUnitCodeIgnoreCaseAndBlockLabelIgnoreCaseAndIdNot(
                normalizedUnitCode,
                normalizedBlockLabel,
                currentUnitId
            );

        if (exists) {
            throw new IllegalArgumentException("A unit with the same code already exists in the selected block.");
        }
    }

    private void applyEditableFields(UnitEntity unit, String unitCode, String blockLabel, Integer floorNumber, String observations) {
        unit.setUnitCode(unitCode.trim());
        unit.setBlockLabel(blockLabel.trim());
        unit.setFloorNumber(floorNumber);
        unit.setObservations(blankToNull(observations));
    }

    private void assignResidents(UnitEntity unit, List<UUID> residentIds) {
        Set<UUID> targetIds = residentIds == null ? Set.of() : new HashSet<>(residentIds);

        List<ResidentEntity> currentResidents = residentRepository.findAllByUnit_IdOrderByLastNameAscFirstNameAsc(unit.getId());
        for (ResidentEntity resident : currentResidents) {
            if (!targetIds.contains(resident.getId())) {
                resident.setUnit(null);
            }
        }

        if (!currentResidents.isEmpty()) {
            residentRepository.saveAll(currentResidents);
        }

        if (targetIds.isEmpty()) {
            return;
        }

        List<ResidentEntity> targetResidents = residentRepository.findAllById(targetIds);
        if (targetResidents.size() != targetIds.size()) {
            throw new ResourceNotFoundException("One or more residents could not be found for the selected unit.");
        }

        for (ResidentEntity resident : targetResidents) {
            resident.setUnit(unit);
        }

        residentRepository.saveAll(targetResidents);
    }

    private UnitResponse toResponse(UnitEntity unit) {
        List<ResidentEntity> residents = residentRepository.findAllByUnit_IdOrderByLastNameAscFirstNameAsc(unit.getId());
        return unitMapper.toResponse(unit, residents);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
