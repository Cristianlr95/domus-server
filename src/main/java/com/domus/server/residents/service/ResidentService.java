package com.domus.server.residents.service;

import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.residents.dto.request.CreateResidentRequest;
import com.domus.server.residents.dto.request.UpdateResidentRequest;
import com.domus.server.residents.dto.request.UpdateResidentStatusRequest;
import com.domus.server.residents.dto.response.ResidentLinkedUserResponse;
import com.domus.server.residents.dto.response.ResidentResponse;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.residents.mapper.ResidentMapper;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.residents.support.ResidentSpecifications;
import com.domus.server.user.entity.RoleName;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;
    private final ResidentMapper residentMapper;

    public ResidentService(ResidentRepository residentRepository, UserRepository userRepository, ResidentMapper residentMapper) {
        this.residentRepository = residentRepository;
        this.userRepository = userRepository;
        this.residentMapper = residentMapper;
    }

    public ResidentResponse create(CreateResidentRequest request) {
        validateUniqueFields(request.documentNumber(), request.email(), request.linkedUserId(), null);

        ResidentEntity resident = new ResidentEntity();
        resident.setId(UUID.randomUUID());
        resident.setActive(true);
        applyEditableFields(
            resident,
            request.firstName(),
            request.lastName(),
            request.documentNumber(),
            request.email(),
            request.phone(),
            request.residentType(),
            request.unitLabel(),
            request.blockLabel(),
            request.linkedUserId()
        );

        return residentMapper.toResponse(residentRepository.save(resident));
    }

    @Transactional(readOnly = true)
    public List<ResidentResponse> list(Boolean active, String search) {
        Specification<ResidentEntity> specification = ResidentSpecifications.withActive(active)
            .and(ResidentSpecifications.search(search));

        return residentRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "lastName", "firstName"))
            .stream()
            .map(residentMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ResidentResponse getById(UUID id) {
        return residentMapper.toResponse(getResident(id));
    }

    public ResidentResponse update(UUID id, UpdateResidentRequest request) {
        ResidentEntity resident = getResident(id);
        validateUniqueFields(request.documentNumber(), request.email(), request.linkedUserId(), resident.getId());

        applyEditableFields(
            resident,
            request.firstName(),
            request.lastName(),
            request.documentNumber(),
            request.email(),
            request.phone(),
            request.residentType(),
            request.unitLabel(),
            request.blockLabel(),
            request.linkedUserId()
        );

        return residentMapper.toResponse(residentRepository.save(resident));
    }

    public ResidentResponse updateStatus(UUID id, UpdateResidentStatusRequest request) {
        ResidentEntity resident = getResident(id);
        resident.setActive(request.active());
        return residentMapper.toResponse(residentRepository.save(resident));
    }

    @Transactional(readOnly = true)
    public List<ResidentLinkedUserResponse> listLinkableUsers(UUID currentUserId) {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName", "lastName"))
            .stream()
            .filter(user -> user.getRoleNames().contains(RoleName.RESIDENTE))
            .filter(user -> currentUserId != null && user.getId().equals(currentUserId) || !residentRepository.existsByLinkedUser_Id(user.getId()))
            .map(residentMapper::toLinkedUser)
            .toList();
    }

    private ResidentEntity getResident(UUID id) {
        return residentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resident not found."));
    }

    private void applyEditableFields(
        ResidentEntity resident,
        String firstName,
        String lastName,
        String documentNumber,
        String email,
        String phone,
        com.domus.server.residents.entity.ResidentType residentType,
        String unitLabel,
        String blockLabel,
        UUID linkedUserId
    ) {
        resident.setFirstName(firstName.trim());
        resident.setLastName(lastName.trim());
        resident.setDocumentNumber(documentNumber.trim());
        resident.setEmail(normalizeEmail(email));
        resident.setPhone(blankToNull(phone));
        resident.setResidentType(residentType);
        resident.setUnitLabel(blankToNull(unitLabel));
        resident.setBlockLabel(blankToNull(blockLabel));
        resident.setLinkedUser(resolveLinkedUser(linkedUserId));
    }

    private void validateUniqueFields(String documentNumber, String email, UUID linkedUserId, UUID residentId) {
        String normalizedDocument = documentNumber.trim();
        if (residentId == null) {
            if (residentRepository.existsByDocumentNumberIgnoreCase(normalizedDocument)) {
                throw new IllegalArgumentException("A resident with the same document already exists.");
            }
        } else if (residentRepository.existsByDocumentNumberIgnoreCaseAndIdNot(normalizedDocument, residentId)) {
            throw new IllegalArgumentException("A resident with the same document already exists.");
        }

        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail != null) {
            if (residentId == null) {
                if (residentRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                    throw new IllegalArgumentException("A resident with the same email already exists.");
                }
            } else if (residentRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, residentId)) {
                throw new IllegalArgumentException("A resident with the same email already exists.");
            }
        }

        if (linkedUserId != null) {
            if (residentId == null) {
                if (residentRepository.existsByLinkedUser_Id(linkedUserId)) {
                    throw new IllegalArgumentException("The selected user is already linked to another resident.");
                }
            } else if (residentRepository.existsByLinkedUser_IdAndIdNot(linkedUserId, residentId)) {
                throw new IllegalArgumentException("The selected user is already linked to another resident.");
            }
        }
    }

    private UserEntity resolveLinkedUser(UUID linkedUserId) {
        if (linkedUserId == null) {
            return null;
        }

        UserEntity user = userRepository.findById(linkedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Linked user not found."));

        if (!user.getRoleNames().contains(RoleName.RESIDENTE)) {
            throw new IllegalArgumentException("The selected user does not have the resident role.");
        }

        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String trimmed = email.trim();
        return trimmed.isBlank() ? null : trimmed.toLowerCase();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
