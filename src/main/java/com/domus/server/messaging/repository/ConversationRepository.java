package com.domus.server.messaging.repository;

import com.domus.server.messaging.entity.ConversationEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    Optional<ConversationEntity> findByParticipantOneIdAndParticipantTwoId(UUID participantOneId, UUID participantTwoId);

    @Query("""
        select c
        from ConversationEntity c
        where c.participantOne.id = :userId or c.participantTwo.id = :userId
        order by c.lastMessageAt desc
        """)
    List<ConversationEntity> findAllForUser(@Param("userId") UUID userId);
}
