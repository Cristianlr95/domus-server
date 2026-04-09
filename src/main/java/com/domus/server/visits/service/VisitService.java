package com.domus.server.visits.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.user.entity.RoleName;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import com.domus.server.visits.dto.request.CreateVisitRequest;
import com.domus.server.visits.dto.request.UpdateVisitRequest;
import com.domus.server.visits.dto.request.UpdateVisitStatusRequest;
import com.domus.server.visits.dto.response.VisitResponse;
import com.domus.server.visits.entity.VisitEntity;
import com.domus.server.visits.entity.VisitRegistrationType;
import com.domus.server.visits.entity.VisitStatus;
import com.domus.server.visits.mapper.VisitMapper;
import com.domus.server.visits.repository.VisitRepository;
import com.domus.server.visits.support.VisitSpecifications;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final VisitMapper visitMapper;

    public VisitService(VisitRepository visitRepository, UserRepository userRepository, VisitMapper visitMapper) {
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.visitMapper = visitMapper;
    }

    public VisitResponse create(CreateVisitRequest request, UUID recordedByUserId) {
        UserEntity recordedByUser = getUser(recordedByUserId, "Recording user not found.");
        UserEntity residentUser = resolveResident(request.residentUserId());

        VisitEntity visit = new VisitEntity();
        visit.setId(UUID.randomUUID());
        applyEditableFields(
            visit,
            request.visitorName(),
            request.visitorDocument(),
            request.visitorPhone(),
            request.vehiclePlate(),
            residentUser,
            request.residentName(),
            request.unitLabel(),
            request.blockLabel(),
            request.observations(),
            request.registrationType()
        );
        visit.setStatus(VisitStatus.PENDIENTE);
        visit.setRecordedByUser(recordedByUser);

        return visitMapper.toResponse(visitRepository.save(visit));
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> list(VisitStatus status, String search) {
        Specification<VisitEntity> specification = VisitSpecifications.withStatus(status)
            .and(VisitSpecifications.search(search));

        return visitRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
            .stream()
            .map(visitMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public VisitResponse getById(UUID id) {
        return visitMapper.toResponse(getVisit(id));
    }

    public VisitResponse update(UUID id, UpdateVisitRequest request) {
        VisitEntity visit = getVisit(id);
        ensureEditable(visit);

        UserEntity residentUser = resolveResident(request.residentUserId());
        applyEditableFields(
            visit,
            request.visitorName(),
            request.visitorDocument(),
            request.visitorPhone(),
            request.vehiclePlate(),
            residentUser,
            request.residentName(),
            request.unitLabel(),
            request.blockLabel(),
            request.observations(),
            request.registrationType()
        );

        return visitMapper.toResponse(visitRepository.save(visit));
    }

    public VisitResponse updateStatus(UUID id, UpdateVisitStatusRequest request) {
        VisitEntity visit = getVisit(id);
        VisitStatus nextStatus = request.status();

        validateTransition(visit.getStatus(), nextStatus);

        if (nextStatus == VisitStatus.INGRESADA && visit.getEntryAt() == null) {
            visit.setEntryAt(Instant.now());
        }

        if (nextStatus == VisitStatus.FINALIZADA) {
            if (visit.getEntryAt() == null) {
                throw new IllegalArgumentException("A visit must be checked in before it can be finalized.");
            }
            if (visit.getExitAt() == null) {
                visit.setExitAt(Instant.now());
            }
        }

        visit.setStatus(nextStatus);
        return visitMapper.toResponse(visitRepository.save(visit));
    }

    private VisitEntity getVisit(UUID id) {
        return visitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Visit not found."));
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
        VisitEntity visit,
        String visitorName,
        String visitorDocument,
        String visitorPhone,
        String vehiclePlate,
        UserEntity residentUser,
        String residentName,
        String unitLabel,
        String blockLabel,
        String observations,
        VisitRegistrationType registrationType
    ) {
        String normalizedResidentName = residentName == null ? "" : residentName.trim();
        String normalizedUnitLabel = unitLabel == null ? "" : unitLabel.trim();
        String normalizedBlockLabel = blockLabel == null ? "" : blockLabel.trim();

        if (residentUser == null && normalizedResidentName.isBlank() && normalizedUnitLabel.isBlank()) {
            throw new IllegalArgumentException("A visit must be associated with a resident or unit.");
        }

        visit.setVisitorName(visitorName.trim());
        visit.setVisitorDocument(visitorDocument.trim());
        visit.setVisitorPhone(blankToNull(visitorPhone));
        visit.setVehiclePlate(blankToNull(vehiclePlate));
        visit.setResidentUser(residentUser);
        visit.setResidentName(
            residentUser != null && normalizedResidentName.isBlank()
                ? residentUser.getFirstName() + " " + residentUser.getLastName()
                : normalizedResidentName
        );
        visit.setUnitLabel(blankToNull(normalizedUnitLabel));
        visit.setBlockLabel(blankToNull(normalizedBlockLabel));
        visit.setObservations(blankToNull(observations));
        visit.setRegistrationType(registrationType == null ? VisitRegistrationType.MANUAL_CONSERJERIA : registrationType);
    }

    private void validateTransition(VisitStatus currentStatus, VisitStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case PENDIENTE -> nextStatus == VisitStatus.INGRESADA || nextStatus == VisitStatus.CANCELADA;
            case INGRESADA -> nextStatus == VisitStatus.FINALIZADA || nextStatus == VisitStatus.CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                "Invalid visit status transition from %s to %s.".formatted(currentStatus, nextStatus)
            );
        }
    }

    private void ensureEditable(VisitEntity visit) {
        if (visit.getStatus() == VisitStatus.FINALIZADA || visit.getStatus() == VisitStatus.CANCELADA) {
            throw new IllegalArgumentException("A finalized or canceled visit cannot be edited.");
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
