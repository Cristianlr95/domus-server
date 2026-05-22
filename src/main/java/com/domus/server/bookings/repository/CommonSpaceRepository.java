package com.domus.server.bookings.repository;

import com.domus.server.bookings.entity.CommonSpaceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonSpaceRepository extends JpaRepository<CommonSpaceEntity, UUID> {

    List<CommonSpaceEntity> findAllByActiveTrueOrderByNameAsc();
}
