package com.domus.server.parking.repository;

import com.domus.server.parking.entity.ParkingEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ParkingRepository extends JpaRepository<ParkingEntity, UUID>, JpaSpecificationExecutor<ParkingEntity> {

    boolean existsBySpotCodeIgnoreCase(String spotCode);

    boolean existsBySpotCodeIgnoreCaseAndIdNot(String spotCode, UUID id);

    long countByActiveTrue();
}
