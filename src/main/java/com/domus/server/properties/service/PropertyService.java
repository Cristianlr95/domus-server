package com.domus.server.properties.service;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.properties.dto.request.CreatePropertyRequest;
import com.domus.server.properties.dto.request.UpdatePropertyRequest;
import com.domus.server.properties.dto.response.PropertyResponse;
import com.domus.server.properties.entity.PropertyEntity;
import com.domus.server.properties.entity.PropertyStatus;
import com.domus.server.properties.entity.PropertyType;
import com.domus.server.properties.mapper.PropertyMapper;
import com.domus.server.properties.repository.PropertyRepository;
import com.domus.server.properties.support.PropertySpecifications;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final AuditLogService auditLogService;

    public PropertyService(
        PropertyRepository propertyRepository,
        PropertyMapper propertyMapper,
        AuditLogService auditLogService
    ) {
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
        this.auditLogService = auditLogService;
    }

    public PropertyResponse create(CreatePropertyRequest request, UUID actorUserId) {
        validateUniqueLabel(request.label(), null);

        PropertyEntity property = new PropertyEntity();
        property.setId(UUID.randomUUID());
        property.setLabel(request.label().trim());
        property.setBlockLabel(blankToNull(request.blockLabel()));
        property.setType(request.type());
        property.setStatus(request.status());
        property.setBedrooms(request.bedrooms());
        property.setBathrooms(request.bathrooms());
        property.setSquareMeters(request.squareMeters());
        property.setFloor(request.floor());
        property.setOwnerName(request.ownerName().trim());
        property.setOwnerEmail(request.ownerEmail().trim().toLowerCase());
        property.setOwnerPhone(blankToNull(request.ownerPhone()));
        property.setResidentsCount(request.residentsCount());
        property.setObservations(blankToNull(request.observations()));
        property.setImageUrl(blankToNull(request.imageUrl()));

        PropertyEntity savedProperty = propertyRepository.save(property);
        PropertyResponse response = propertyMapper.toResponse(savedProperty);
        auditLogService.record(
            actorUserId,
            "PROPERTY",
            savedProperty.getId().toString(),
            AuditAction.CREATE,
            "Property " + savedProperty.getLabel() + " created.",
            null,
            response,
            java.util.Map.of("type", savedProperty.getType(), "status", savedProperty.getStatus())
        );
        return response;
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> list(PropertyType type, PropertyStatus status, String block, String search) {
        Specification<PropertyEntity> specification = PropertySpecifications.withType(type)
            .and(PropertySpecifications.withStatus(status))
            .and(PropertySpecifications.withBlock(block))
            .and(PropertySpecifications.search(search));

        return propertyRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "label"))
            .stream()
            .map(propertyMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse getById(UUID id) {
        return propertyMapper.toResponse(getProperty(id));
    }

    public PropertyResponse update(UUID id, UpdatePropertyRequest request, UUID actorUserId) {
        PropertyEntity property = getProperty(id);
        PropertyResponse previousState = propertyMapper.toResponse(property);

        if (request.status() != null) {
            property.setStatus(request.status());
        }
        if (request.ownerName() != null) {
            property.setOwnerName(requiredTrim(request.ownerName(), "Owner name cannot be blank."));
        }
        if (request.ownerEmail() != null) {
            property.setOwnerEmail(requiredTrim(request.ownerEmail(), "Owner email cannot be blank.").toLowerCase());
        }
        if (request.ownerPhone() != null) {
            property.setOwnerPhone(blankToNull(request.ownerPhone()));
        }
        if (request.residentsCount() != null) {
            property.setResidentsCount(request.residentsCount());
        }
        if (request.observations() != null) {
            property.setObservations(blankToNull(request.observations()));
        }

        PropertyEntity savedProperty = propertyRepository.save(property);
        PropertyResponse response = propertyMapper.toResponse(savedProperty);
        AuditAction action = previousState.status() != response.status() ? AuditAction.STATUS_CHANGE : AuditAction.UPDATE;
        auditLogService.record(
            actorUserId,
            "PROPERTY",
            savedProperty.getId().toString(),
            action,
            "Property " + savedProperty.getLabel() + " updated.",
            previousState,
            response,
            null
        );
        return response;
    }

    public void delete(UUID id, UUID actorUserId) {
        PropertyEntity property = getProperty(id);
        PropertyResponse previousState = propertyMapper.toResponse(property);
        propertyRepository.delete(property);
        auditLogService.record(
            actorUserId,
            "PROPERTY",
            property.getId().toString(),
            AuditAction.DELETE,
            "Property " + property.getLabel() + " deleted.",
            previousState,
            null,
            null
        );
    }

    private PropertyEntity getProperty(UUID id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Property not found."));
    }

    private void validateUniqueLabel(String label, UUID currentId) {
        String normalizedLabel = label.trim();
        boolean exists = currentId == null
            ? propertyRepository.existsByLabelIgnoreCase(normalizedLabel)
            : propertyRepository.existsByLabelIgnoreCaseAndIdNot(normalizedLabel, currentId);

        if (exists) {
            throw new IllegalArgumentException("A property with the same label already exists.");
        }
    }

    private String requiredTrim(String value, String message) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
