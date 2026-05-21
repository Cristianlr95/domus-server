package com.domus.server.user.repository;

import com.domus.server.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @Query("""
        select u
        from UserEntity u
        where u.active = true and u.id <> :currentUserId
        order by u.firstName asc, u.lastName asc, u.email asc
        """)
    List<UserEntity> findAllActiveExcluding(@Param("currentUserId") UUID currentUserId);
}
