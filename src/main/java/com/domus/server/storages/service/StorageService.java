package com.domus.server.storages.service;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.storages.dto.request.CreateStorageRequest;
import com.domus.server.storages.dto.request.UpdateStorageRequest;
import com.domus.server.storages.dto.request.UpdateStorageStatusRequest;
import com.domus.server.storages.dto.response.StorageResponse;
import com.domus.server.storages.entity.StorageEntity;
import com.domus.server.storages.entity.StorageOccupancyStatus;
import com.domus.server.storages.mapper.StorageMapper;
import com.domus.server.storages.repository.StorageRepository;
import com.domus.server.storages.support.StorageSpecifications;
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
public class StorageService {

    private final StorageRepository storageRepository;
    private final UnitRepository unitRepository;
    private final StorageMapper storageMapper;
    private final AuditLogService auditLogService;

    public StorageService(
        StorageRepository storageRepository,
        UnitRepository unitRepository,
        StorageMapper storageMapper,
        AuditLogService auditLogService
    ) {
        this.storageRepository = storageRepository;
        this.unitRepository = unitRepository;
        this.storageMapper = storageMapper;
        this.auditLogService = auditLogService;
    }

    public StorageResponse create(CreateStorageRequest request, UUID actorUserId) {
        validateUniqueStorageCode(request.storageCode(), null);

        StorageEntity storage = new StorageEntity();
        storage.setId(UUID.randomUUID());
        storage.setActive(true);
        applyEditableFields(
            storage,
            request.storageCode(),
            request.storageType(),
            request.occupancyStatus(),
            request.unitId(),
            request.observations()
        );

        StorageEntity savedStorage = storageRepository.save(storage);
        StorageResponse response = storageMapper.toResponse(savedStorage);
        auditLogService.record(
            actorUserId,
            "STORAGE",
            savedStorage.getId().toString(),
            AuditAction.CREATE,
            "Storage " + savedStorage.getStorageCode() + " created.",
            null,
            response,
            java.util.Map.of("storageType", savedStorage.getStorageType())
        );
        return response;
    }

    @Transactional(readOnly = true)
    public List<StorageResponse> list(Boolean active, StorageOccupancyStatus occupancyStatus, UUID unitId, String search) {
        Specification<StorageEntity> specification = StorageSpecifications.withActive(active)
            .and(StorageSpecifications.withOccupancyStatus(occupancyStatus))
            .and(StorageSpecifications.withUnitId(unitId))
            .and(StorageSpecifications.search(search));

        return storageRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "storageCode"))
            .stream()
            .map(storageMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StorageResponse getById(UUID id) {
        return storageMapper.toResponse(getStorage(id));
    }

    public StorageResponse update(UUID id, UpdateStorageRequest request, UUID actorUserId) {
        StorageEntity storage = getStorage(id);
        validateUniqueStorageCode(request.storageCode(), storage.getId());
        StorageResponse previousState = storageMapper.toResponse(storage);

        applyEditableFields(
            storage,
            request.storageCode(),
            request.storageType(),
            request.occupancyStatus(),
            request.unitId(),
            request.observations()
        );

        StorageEntity savedStorage = storageRepository.save(storage);
        StorageResponse response = storageMapper.toResponse(savedStorage);
        auditLogService.record(
            actorUserId,
            "STORAGE",
            savedStorage.getId().toString(),
            AuditAction.UPDATE,
            "Storage " + savedStorage.getStorageCode() + " updated.",
            previousState,
            response,
            null
        );
        return response;
    }

    public StorageResponse updateStatus(UUID id, UpdateStorageStatusRequest request, UUID actorUserId) {
        StorageEntity storage = getStorage(id);
        StorageOccupancyStatus previousStatus = storage.getOccupancyStatus();
        StorageResponse previousState = storageMapper.toResponse(storage);

        if (!request.active() && request.occupancyStatus() == StorageOccupancyStatus.OCUPADA) {
            throw new IllegalArgumentException("An inactive storage cannot be marked as occupied.");
        }

        storage.setActive(request.active());
        storage.setOccupancyStatus(request.occupancyStatus());
        StorageEntity savedStorage = storageRepository.save(storage);
        StorageResponse response = storageMapper.toResponse(savedStorage);
        auditLogService.record(
            actorUserId,
            "STORAGE",
            savedStorage.getId().toString(),
            AuditAction.STATUS_CHANGE,
            "Storage " + savedStorage.getStorageCode() + " status changed from " + previousStatus + " to " + request.occupancyStatus() + ".",
            previousState,
            response,
            java.util.Map.of("previousStatus", previousStatus, "newStatus", request.occupancyStatus())
        );
        return response;
    }

    private StorageEntity getStorage(UUID id) {
        return storageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Storage not found."));
    }

    private void validateUniqueStorageCode(String storageCode, UUID currentId) {
        String normalizedStorageCode = storageCode.trim();
        boolean exists = currentId == null
            ? storageRepository.existsByStorageCodeIgnoreCase(normalizedStorageCode)
            : storageRepository.existsByStorageCodeIgnoreCaseAndIdNot(normalizedStorageCode, currentId);

        if (exists) {
            throw new IllegalArgumentException("A storage with the same code already exists.");
        }
    }

    private void applyEditableFields(
        StorageEntity storage,
        String storageCode,
        com.domus.server.storages.entity.StorageType storageType,
        StorageOccupancyStatus occupancyStatus,
        UUID unitId,
        String observations
    ) {
        if (!storage.isActive() && occupancyStatus == StorageOccupancyStatus.OCUPADA) {
            throw new IllegalArgumentException("An inactive storage cannot be marked as occupied.");
        }

        storage.setStorageCode(storageCode.trim());
        storage.setStorageType(storageType);
        storage.setOccupancyStatus(occupancyStatus);
        storage.setUnit(resolveUnit(unitId));
        storage.setObservations(blankToNull(observations));
    }

    private UnitEntity resolveUnit(UUID unitId) {
        return unitRepository.findById(unitId)
            .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

}
