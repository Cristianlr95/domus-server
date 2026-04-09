package com.domus.server.visits.repository;

import com.domus.server.visits.entity.VisitEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VisitRepository extends JpaRepository<VisitEntity, UUID>, JpaSpecificationExecutor<VisitEntity> {
}
