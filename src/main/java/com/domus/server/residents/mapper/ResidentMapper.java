package com.domus.server.residents.mapper;

import com.domus.server.residents.dto.response.ResidentLinkedUserResponse;
import com.domus.server.residents.dto.response.ResidentResponse;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class ResidentMapper {

    public ResidentResponse toResponse(ResidentEntity resident) {
        return new ResidentResponse(
            resident.getId(),
            resident.getFirstName(),
            resident.getLastName(),
            resident.getDocumentNumber(),
            resident.getEmail(),
            resident.getPhone(),
            resident.isActive(),
            resident.getResidentType(),
            resident.getUnitLabel(),
            resident.getBlockLabel(),
            resident.getCreatedAt(),
            resident.getUpdatedAt(),
            toLinkedUser(resident.getLinkedUser())
        );
    }

    public ResidentLinkedUserResponse toLinkedUser(UserEntity user) {
        if (user == null) {
            return null;
        }

        return new ResidentLinkedUserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail()
        );
    }
}
