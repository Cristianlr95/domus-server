package com.domus.server.units.repository;

import com.domus.server.units.entity.UnitEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnitRepository extends JpaRepository<UnitEntity, UUID>, JpaSpecificationExecutor<UnitEntity> {

    boolean existsByUnitCodeIgnoreCaseAndBlockLabelIgnoreCase(String unitCode, String blockLabel);

    boolean existsByUnitCodeIgnoreCaseAndBlockLabelIgnoreCaseAndIdNot(String unitCode, String blockLabel, UUID id);
}
