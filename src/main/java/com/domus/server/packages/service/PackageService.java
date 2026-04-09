package com.domus.server.packages.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.packages.dto.request.CreatePackageRequest;
import com.domus.server.packages.dto.request.DeliverPackageRequest;
import com.domus.server.packages.dto.request.UpdatePackageRequest;
import com.domus.server.packages.dto.request.UpdatePackageStatusRequest;
import com.domus.server.packages.dto.response.PackageResponse;
import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.packages.entity.PackageStatus;
import com.domus.server.packages.entity.PackageType;
import com.domus.server.packages.mapper.PackageMapper;
import com.domus.server.packages.repository.PackageRepository;
import com.domus.server.packages.support.PackageSpecifications;
import com.domus.server.user.entity.RoleName;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PackageService {

    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final PackageMapper packageMapper;

    public PackageService(PackageRepository packageRepository, UserRepository userRepository, PackageMapper packageMapper) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.packageMapper = packageMapper;
    }

    public PackageResponse create(CreatePackageRequest request, UUID recordedByUserId) {
        UserEntity recordedByUser = getUser(recordedByUserId, "Recording user not found.");
        UserEntity residentUser = resolveResident(request.residentUserId());

        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setId(UUID.randomUUID());
        applyEditableFields(
            packageEntity,
            request.description(),
            request.senderName(),
            request.packageType(),
            residentUser,
            request.residentName(),
            request.unitLabel(),
            request.blockLabel(),
            request.receivedAt(),
            request.observations(),
            request.receivedByName()
        );
        packageEntity.setStatus(PackageStatus.RECIBIDA);
        packageEntity.setRecordedByUser(recordedByUser);

        return packageMapper.toResponse(packageRepository.save(packageEntity));
    }

    @Transactional(readOnly = true)
    public List<PackageResponse> list(PackageStatus status, String search) {
        Specification<PackageEntity> specification = PackageSpecifications.withStatus(status)
            .and(PackageSpecifications.search(search));

        return packageRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "receivedAt"))
            .stream()
            .map(packageMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PackageResponse getById(UUID id) {
        return packageMapper.toResponse(getPackage(id));
    }

    public PackageResponse update(UUID id, UpdatePackageRequest request) {
        PackageEntity packageEntity = getPackage(id);
        ensureEditable(packageEntity);

        UserEntity residentUser = resolveResident(request.residentUserId());
        applyEditableFields(
            packageEntity,
            request.description(),
            request.senderName(),
            request.packageType(),
            residentUser,
            request.residentName(),
            request.unitLabel(),
            request.blockLabel(),
            request.receivedAt(),
            request.observations(),
            request.receivedByName()
        );

        return packageMapper.toResponse(packageRepository.save(packageEntity));
    }

    public PackageResponse updateStatus(UUID id, UpdatePackageStatusRequest request) {
        PackageEntity packageEntity = getPackage(id);
        PackageStatus nextStatus = request.status();

        validateTransition(packageEntity.getStatus(), nextStatus);

        if (nextStatus == PackageStatus.ENTREGADA) {
            throw new IllegalArgumentException("Use the delivery action to mark a package as delivered.");
        }

        packageEntity.setStatus(nextStatus);
        return packageMapper.toResponse(packageRepository.save(packageEntity));
    }

    public PackageResponse deliver(UUID id, DeliverPackageRequest request) {
        PackageEntity packageEntity = getPackage(id);

        if (packageEntity.getStatus() == PackageStatus.ENTREGADA) {
            throw new IllegalArgumentException("The package has already been delivered.");
        }

        if (packageEntity.getStatus() == PackageStatus.CANCELADA) {
            throw new IllegalArgumentException("A canceled package cannot be delivered.");
        }

        packageEntity.setDeliveredToName(request.deliveredToName().trim());
        packageEntity.setDeliveredAt(request.deliveredAt() == null ? Instant.now() : request.deliveredAt());
        packageEntity.setStatus(PackageStatus.ENTREGADA);

        return packageMapper.toResponse(packageRepository.save(packageEntity));
    }

    private PackageEntity getPackage(UUID id) {
        return packageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found."));
    }

    private UserEntity getUser(UUID id, String message) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private UserEntity resolveResident(UUID residentUserId) {
        if (residentUserId == null) {
            return null;
        }

        UserEntity resident = getUser(residentUserId, "Resident user not found.");
        if (!resident.getRoleNames().contains(RoleName.RESIDENTE)) {
            throw new IllegalArgumentException("The selected user is not a resident.");
        }

        return resident;
    }

    private void applyEditableFields(
        PackageEntity packageEntity,
        String description,
        String senderName,
        PackageType packageType,
        UserEntity residentUser,
        String residentName,
        String unitLabel,
        String blockLabel,
        Instant receivedAt,
        String observations,
        String receivedByName
    ) {
        String normalizedResidentName = residentName == null ? "" : residentName.trim();
        String normalizedUnitLabel = unitLabel == null ? "" : unitLabel.trim();
        String normalizedBlockLabel = blockLabel == null ? "" : blockLabel.trim();

        if (residentUser == null && normalizedResidentName.isBlank() && normalizedUnitLabel.isBlank()) {
            throw new IllegalArgumentException("A package must be associated with a resident or unit.");
        }

        packageEntity.setDescription(description.trim());
        packageEntity.setSenderName(blankToNull(senderName));
        packageEntity.setPackageType(packageType);
        packageEntity.setResidentUser(residentUser);
        packageEntity.setResidentName(
            residentUser != null && normalizedResidentName.isBlank()
                ? residentUser.getFirstName() + " " + residentUser.getLastName()
                : normalizedResidentName
        );
        packageEntity.setUnitLabel(blankToNull(normalizedUnitLabel));
        packageEntity.setBlockLabel(blankToNull(normalizedBlockLabel));
        packageEntity.setReceivedAt(receivedAt == null ? Instant.now() : receivedAt);
        packageEntity.setObservations(blankToNull(observations));
        packageEntity.setReceivedByName(blankToNull(receivedByName));
    }

    private void validateTransition(PackageStatus currentStatus, PackageStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case RECIBIDA -> nextStatus == PackageStatus.NOTIFICADA || nextStatus == PackageStatus.CANCELADA;
            case NOTIFICADA -> nextStatus == PackageStatus.CANCELADA;
            case ENTREGADA, CANCELADA -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                "Invalid package status transition from %s to %s.".formatted(currentStatus, nextStatus)
            );
        }
    }

    private void ensureEditable(PackageEntity packageEntity) {
        if (packageEntity.getStatus() == PackageStatus.ENTREGADA || packageEntity.getStatus() == PackageStatus.CANCELADA) {
            throw new IllegalArgumentException("A delivered or canceled package cannot be edited.");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
