package com.domus.server.residents.repository;

import com.domus.server.residents.entity.ResidentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResidentRepository extends JpaRepository<ResidentEntity, UUID>, JpaSpecificationExecutor<ResidentEntity> {

    long countByActiveTrue();

    boolean existsByDocumentNumberIgnoreCase(String documentNumber);

    boolean existsByDocumentNumberIgnoreCaseAndIdNot(String documentNumber, UUID id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByLinkedUser_Id(UUID linkedUserId);

    boolean existsByLinkedUser_IdAndIdNot(UUID linkedUserId, UUID id);

    List<ResidentEntity> findAllByUnit_IdOrderByLastNameAscFirstNameAsc(UUID unitId);

    Optional<ResidentEntity> findByLinkedUser_Id(UUID linkedUserId);
}
