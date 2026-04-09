package com.domus.server.storages.repository;

import com.domus.server.storages.entity.StorageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StorageRepository extends JpaRepository<StorageEntity, UUID>, JpaSpecificationExecutor<StorageEntity> {

    boolean existsByStorageCodeIgnoreCase(String storageCode);

    boolean existsByStorageCodeIgnoreCaseAndIdNot(String storageCode, UUID id);
}
