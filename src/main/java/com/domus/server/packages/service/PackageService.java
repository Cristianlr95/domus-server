package com.domus.server.packages.service;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
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
import com.domus.server.notifications.service.NotificationService;
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
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PackageService(
        PackageRepository packageRepository,
        UserRepository userRepository,
        PackageMapper packageMapper,
        NotificationService notificationService,
        AuditLogService auditLogService
    ) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.packageMapper = packageMapper;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
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

        PackageEntity savedPackage = packageRepository.save(packageEntity);
        notificationService.notifyPackageReceived(savedPackage);
        PackageResponse response = packageMapper.toResponse(savedPackage);
        auditLogService.record(
            recordedByUserId,
            "PACKAGE",
            savedPackage.getId().toString(),
            AuditAction.CREATE,
            "Package registered: " + savedPackage.getDescription() + ".",
            null,
            response,
            java.util.Map.of("status", savedPackage.getStatus())
        );
        return response;
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

    public PackageResponse update(UUID id, UpdatePackageRequest request, UUID actorUserId) {
        PackageEntity packageEntity = getPackage(id);
        ensureEditable(packageEntity);
        PackageResponse previousState = packageMapper.toResponse(packageEntity);

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

        PackageEntity savedPackage = packageRepository.save(packageEntity);
        PackageResponse response = packageMapper.toResponse(savedPackage);
        auditLogService.record(
            actorUserId,
            "PACKAGE",
            savedPackage.getId().toString(),
            AuditAction.UPDATE,
            "Package information updated: " + savedPackage.getDescription() + ".",
            previousState,
            response,
            null
        );
        return response;
    }

    public PackageResponse updateStatus(UUID id, UpdatePackageStatusRequest request, UUID actorUserId) {
        PackageEntity packageEntity = getPackage(id);
        PackageStatus nextStatus = request.status();
        PackageResponse previousState = packageMapper.toResponse(packageEntity);
        PackageStatus previousStatus = packageEntity.getStatus();

        validateTransition(packageEntity.getStatus(), nextStatus);

        if (nextStatus == PackageStatus.ENTREGADA) {
            throw new IllegalArgumentException("Use the delivery action to mark a package as delivered.");
        }

        packageEntity.setStatus(nextStatus);
        PackageEntity savedPackage = packageRepository.save(packageEntity);
        PackageResponse response = packageMapper.toResponse(savedPackage);
        auditLogService.record(
            actorUserId,
            "PACKAGE",
            savedPackage.getId().toString(),
            AuditAction.STATUS_CHANGE,
            "Package status changed from " + previousStatus + " to " + nextStatus + ".",
            previousState,
            response,
            java.util.Map.of("previousStatus", previousStatus, "newStatus", nextStatus)
        );
        return response;
    }

    public PackageResponse deliver(UUID id, DeliverPackageRequest request, UUID actorUserId) {
        PackageEntity packageEntity = getPackage(id);
        PackageResponse previousState = packageMapper.toResponse(packageEntity);

        if (packageEntity.getStatus() == PackageStatus.ENTREGADA) {
            throw new IllegalArgumentException("The package has already been delivered.");
        }

        if (packageEntity.getStatus() == PackageStatus.CANCELADA) {
            throw new IllegalArgumentException("A canceled package cannot be delivered.");
        }

        packageEntity.setDeliveredToName(request.deliveredToName().trim());
        packageEntity.setDeliveredAt(request.deliveredAt() == null ? Instant.now() : request.deliveredAt());
        packageEntity.setStatus(PackageStatus.ENTREGADA);

        PackageEntity savedPackage = packageRepository.save(packageEntity);
        PackageResponse response = packageMapper.toResponse(savedPackage);
        auditLogService.record(
            actorUserId,
            "PACKAGE",
            savedPackage.getId().toString(),
            AuditAction.DELIVERY,
            "Package delivered to " + savedPackage.getDeliveredToName() + ".",
            previousState,
            response,
            java.util.Map.of("deliveredToName", savedPackage.getDeliveredToName())
        );
        return response;
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
