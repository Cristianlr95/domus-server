package com.domus.server.packages.repository;

import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.packages.entity.PackageStatus;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PackageRepository extends JpaRepository<PackageEntity, UUID>, JpaSpecificationExecutor<PackageEntity> {

    long countByStatusIn(Collection<PackageStatus> statuses);
}
