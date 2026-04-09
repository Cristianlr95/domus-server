package com.domus.server.user.repository;

import com.domus.server.user.entity.PermissionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
}
