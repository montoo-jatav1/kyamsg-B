package com.kyamsg.backend.repository;

import com.kyamsg.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByUserOneIdAndUserTwoId(UUID userOneId, UUID userTwoId);

    @Query("select c from Conversation c where c.userOneId = :userId or c.userTwoId = :userId " +
            "order by c.lastMessageAt desc nulls last, c.createdAt desc")
    List<Conversation> findAllForUser(@Param("userId") UUID userId);
}
